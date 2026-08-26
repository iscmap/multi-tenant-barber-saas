param(
    [string]$AwsProfile = "barber-dev",
    [string]$AwsRegion = "us-east-1",
    [switch]$Force
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")

$NetworkStack = "barber-saas-dev-network"
$EcrStack = "barber-saas-dev-ecr"
$EksStack = "barber-saas-dev-eks"
$ApplicationStack = "barber-saas-dev-application"
$ClusterName = "barber-saas-dev"

$BookingRepository = "barber-saas/dev/booking-service"
$AvailabilityRepository = "barber-saas/dev/availability-service"

$KubernetesAws = Join-Path $ProjectRoot "infra\kubernetes\aws"

function Invoke-AwsSafe {
    param(
        [scriptblock]$Command
    )

    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    & $Command

    $ExitCode = $LASTEXITCODE

    $ErrorActionPreference = $PreviousErrorActionPreference

    return $ExitCode
}

function Test-StackExists {
    param([string]$StackName)

    $ExitCode = Invoke-AwsSafe {
        aws cloudformation describe-stacks `
            --stack-name $StackName `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    }

    return $ExitCode -eq 0
}

function Test-EksClusterExists {

    $ExitCode = Invoke-AwsSafe {
        aws eks describe-cluster `
            --name $ClusterName `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    }

    return $ExitCode -eq 0
}

function Test-EksAddonExists {
    param([string]$AddonName)

    if (-not (Test-EksClusterExists)) {
        return $false
    }

    $ExitCode = Invoke-AwsSafe {
        aws eks describe-addon `
            --cluster-name $ClusterName `
            --addon-name $AddonName `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    }

    return $ExitCode -eq 0
}

function Delete-Stack {
    param([string]$StackName)

    if (-not (Test-StackExists $StackName)) {
        Write-Host "$StackName does not exist."
        return
    }

    Write-Host "Deleting $StackName..."

    aws cloudformation delete-stack `
        --stack-name $StackName `
        --region $AwsRegion `
        --profile $AwsProfile

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start deletion of $StackName."
    }

    aws cloudformation wait stack-delete-complete `
        --stack-name $StackName `
        --region $AwsRegion `
        --profile $AwsProfile

    if ($LASTEXITCODE -ne 0) {
        throw "Failed while waiting for deletion of $StackName."
    }

    Write-Host "$StackName deleted."
}

function Test-EcrRepositoryExists {
    param([string]$RepositoryName)

    $ExitCode = Invoke-AwsSafe {
        aws ecr describe-repositories `
            --repository-names $RepositoryName `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    }

    return $ExitCode -eq 0
}

function Remove-AllEcrImages {
    param([string]$RepositoryName)

    if (-not (Test-EcrRepositoryExists $RepositoryName)) {
        Write-Host "$RepositoryName does not exist."
        return
    }

    Write-Host "Removing images from $RepositoryName..."

    while ($true) {

        $PreviousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = "Continue"

        $ImageIdsJson = aws ecr list-images `
            --repository-name $RepositoryName `
            --max-items 100 `
            --query "imageIds" `
            --output json `
            --region $AwsRegion `
            --profile $AwsProfile

        $ExitCode = $LASTEXITCODE

        $ErrorActionPreference = $PreviousErrorActionPreference

        if ($ExitCode -ne 0) {
            throw "Failed to list images from $RepositoryName."
        }

        $ImageIds = $ImageIdsJson | ConvertFrom-Json

        if ($null -eq $ImageIds -or $ImageIds.Count -eq 0) {
            break
        }

        $TempFile = Join-Path `
            $env:TEMP `
            ("barber-ecr-images-" + [guid]::NewGuid().ToString("N") + ".json")

        @{
            imageIds = @($ImageIds)
        } |
            ConvertTo-Json -Depth 10 |
            Set-Content `
                -Path $TempFile `
                -Encoding ascii

        aws ecr batch-delete-image `
            --repository-name $RepositoryName `
            --cli-input-json "file://$TempFile" `
            --region $AwsRegion `
            --profile $AwsProfile

        $DeleteExitCode = $LASTEXITCODE

        Remove-Item `
            $TempFile `
            -Force `
            -ErrorAction SilentlyContinue

        if ($DeleteExitCode -ne 0) {
            throw "Failed to delete images from $RepositoryName."
        }
    }

    Write-Host "$RepositoryName emptied."
}

Write-Host ""
Write-Host "=== Barber SaaS DEV Environment Destruction ==="
Write-Host ""

Write-Host "This will delete:"
Write-Host "- Kubernetes workloads"
Write-Host "- PostgreSQL and Kafka running inside EKS"
Write-Host "- EKS cluster and node group"
Write-Host "- EKS Pod Identity addon"
Write-Host "- SNS / SQS / DynamoDB application resources"
Write-Host "- NAT Gateway"
Write-Host "- VPC networking"
Write-Host "- ECR repositories"
Write-Host "- ALL Docker images stored in ECR"
Write-Host ""

if (-not $Force) {

    $Confirmation = Read-Host "Type DELETE to continue"

    if ($Confirmation -ne "DELETE") {
        Write-Host "Cancelled."
        exit 0
    }
}

$env:AWS_PROFILE = $AwsProfile
$env:AWS_REGION = $AwsRegion

aws sts get-caller-identity `
    --profile $AwsProfile `
    *> $null

if ($LASTEXITCODE -ne 0) {
    throw "AWS authentication failed."
}

Write-Host ""
Write-Host "1. Kubernetes resources"

if (Test-EksClusterExists) {

    aws eks update-kubeconfig `
        --name $ClusterName `
        --region $AwsRegion `
        --profile $AwsProfile `
        *> $null

    if ($LASTEXITCODE -eq 0) {

        kubectl delete -f `
            (Join-Path $KubernetesAws "booking-deployment.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "availability-deployment.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "kafka-init-job.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "kafka.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "postgres.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete secret barber-saas-db-secret `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "configmap.yaml") `
            --ignore-not-found=true `
            2>$null

        kubectl delete -f `
            (Join-Path $KubernetesAws "service-account.yaml") `
            --ignore-not-found=true `
            2>$null
    }
}
else {
    Write-Host "EKS cluster does not exist."
}

Write-Host ""
Write-Host "2. Application AWS resources"

Delete-Stack $ApplicationStack

Write-Host ""
Write-Host "3. Pod Identity addon"

$AddonName = "eks-pod-identity-agent"

if (Test-EksAddonExists $AddonName) {

    aws eks delete-addon `
        --cluster-name $ClusterName `
        --addon-name $AddonName `
        --region $AwsRegion `
        --profile $AwsProfile `
        *> $null

    if ($LASTEXITCODE -ne 0) {
        throw "Failed to start deletion of Pod Identity addon."
    }

    Write-Host "Waiting for Pod Identity addon deletion..."

    do {
        Start-Sleep -Seconds 10
        $AddonStillExists = Test-EksAddonExists $AddonName
    }
    while ($AddonStillExists)

    Write-Host "Pod Identity addon deleted."
}
else {
    Write-Host "Pod Identity addon does not exist."
}

Write-Host ""
Write-Host "4. EKS"

Delete-Stack $EksStack

Write-Host ""
Write-Host "5. Network"

Delete-Stack $NetworkStack

Write-Host ""
Write-Host "6. ECR"

Remove-AllEcrImages $BookingRepository
Remove-AllEcrImages $AvailabilityRepository

Delete-Stack $EcrStack

Write-Host ""
Write-Host "7. Verify runtime resources"

if (Test-EksClusterExists) {
    Write-Warning "EKS cluster still exists."
}
else {
    Write-Host "EKS cluster deleted."
}

if (Test-StackExists $ApplicationStack) {
    Write-Warning "Application stack still exists."
}
else {
    Write-Host "Application stack deleted."
}

if (Test-StackExists $EksStack) {
    Write-Warning "EKS stack still exists."
}
else {
    Write-Host "EKS stack deleted."
}

if (Test-StackExists $NetworkStack) {
    Write-Warning "Network stack still exists."
}
else {
    Write-Host "Network stack deleted."
}

if (Test-StackExists $EcrStack) {
    Write-Warning "ECR stack still exists."
}
else {
    Write-Host "ECR stack deleted."
}

Write-Host ""
Write-Host "8. Final billable resource check"

Write-Host ""
Write-Host "NAT Gateways:"
aws ec2 describe-nat-gateways `
    --filter "Name=state,Values=pending,available,deleting" `
    --query "NatGateways[].[NatGatewayId,State,VpcId]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "Running EC2 instances:"
aws ec2 describe-instances `
    --filters "Name=instance-state-name,Values=pending,running" `
    --query "Reservations[].Instances[].[InstanceId,InstanceType,State.Name]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "Elastic IP addresses:"
aws ec2 describe-addresses `
    --query "Addresses[].[AllocationId,PublicIp,AssociationId]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "EBS volumes:"
aws ec2 describe-volumes `
    --filters "Name=status,Values=available,in-use" `
    --query "Volumes[].[VolumeId,Size,VolumeType,State]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "DEV environment fully removed."
