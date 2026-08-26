param(
    [string]$AwsProfile = "barber-dev",
    [string]$AwsRegion = "us-east-1",
    [string]$ImageTag = "9.5.5"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")

$NetworkStack = "barber-saas-dev-network"
$EcrStack = "barber-saas-dev-ecr"
$EksStack = "barber-saas-dev-eks"
$ApplicationStack = "barber-saas-dev-application"

$ClusterName = "barber-saas-dev"

$NetworkTemplate = Join-Path $ProjectRoot "infra\aws\network\vpc.yaml"
$EcrTemplate = Join-Path $ProjectRoot "infra\aws\ecr\repositories.yaml"
$EksTemplate = Join-Path $ProjectRoot "infra\aws\eks\cluster.yaml"
$ApplicationTemplate = Join-Path $ProjectRoot "infra\aws\application\resources.yaml"

$EcrLifecyclePolicy = Join-Path $ProjectRoot "infra\aws\ecr\lifecycle-policy.json"

$KubernetesAws = Join-Path $ProjectRoot "infra\kubernetes\aws"

$BookingRepository = "barber-saas/dev/booking-service"
$AvailabilityRepository = "barber-saas/dev/availability-service"

function Invoke-CheckedCommand {
    param(
        [scriptblock]$Command,
        [string]$ErrorMessage
    )

    & $Command

    if ($LASTEXITCODE -ne 0) {
        throw $ErrorMessage
    }
}

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

function Deploy-Stack {
    param(
        [string]$StackName,
        [string]$TemplateFile,
        [switch]$Iam
    )

    if (-not (Test-Path $TemplateFile)) {
        throw "Template not found: $TemplateFile"
    }

    Write-Host "Deploying $StackName..."

    if ($Iam) {

        Invoke-CheckedCommand `
            {
                aws cloudformation deploy `
                    --stack-name $StackName `
                    --template-file $TemplateFile `
                    --capabilities CAPABILITY_NAMED_IAM `
                    --region $AwsRegion `
                    --profile $AwsProfile `
                    --no-fail-on-empty-changeset
            } `
            "Failed to deploy $StackName."
    }
    else {

        Invoke-CheckedCommand `
            {
                aws cloudformation deploy `
                    --stack-name $StackName `
                    --template-file $TemplateFile `
                    --region $AwsRegion `
                    --profile $AwsProfile `
                    --no-fail-on-empty-changeset
            } `
            "Failed to deploy $StackName."
    }
}

function Get-StackOutput {
    param(
        [string]$StackName,
        [string]$OutputKey
    )

    $Value = aws cloudformation describe-stacks `
        --stack-name $StackName `
        --query "Stacks[0].Outputs[?OutputKey=='$OutputKey'].OutputValue | [0]" `
        --output text `
        --region $AwsRegion `
        --profile $AwsProfile

    if (
        $LASTEXITCODE -ne 0 -or
        [string]::IsNullOrWhiteSpace($Value) -or
        $Value -eq "None"
    ) {
        throw "$OutputKey was not found in stack $StackName."
    }

    return $Value
}

Write-Host ""
Write-Host "=== Barber SaaS DEV Environment Creation ==="
Write-Host ""

$env:AWS_PROFILE = $AwsProfile
$env:AWS_REGION = $AwsRegion

Set-Location $ProjectRoot

Invoke-CheckedCommand `
    {
        aws sts get-caller-identity `
            --profile $AwsProfile `
            *> $null
    } `
    "AWS authentication failed."

Write-Host ""
Write-Host "1. Network"

if (-not (Test-StackExists $NetworkStack)) {

    Deploy-Stack `
        -StackName $NetworkStack `
        -TemplateFile $NetworkTemplate
}
else {
    Write-Host "$NetworkStack already exists."
}

Write-Host ""
Write-Host "2. Read network outputs"

$PrivateSubnet1Id = Get-StackOutput `
    -StackName $NetworkStack `
    -OutputKey "PrivateSubnet1Id"

$PrivateSubnet2Id = Get-StackOutput `
    -StackName $NetworkStack `
    -OutputKey "PrivateSubnet2Id"

Write-Host "PrivateSubnet1Id: $PrivateSubnet1Id"
Write-Host "PrivateSubnet2Id: $PrivateSubnet2Id"

Write-Host ""
Write-Host "3. ECR"

if (-not (Test-StackExists $EcrStack)) {

    Deploy-Stack `
        -StackName $EcrStack `
        -TemplateFile $EcrTemplate
}
else {
    Write-Host "$EcrStack already exists."
}

Write-Host ""
Write-Host "4. ECR lifecycle policies"

if (Test-Path $EcrLifecyclePolicy) {

    Invoke-CheckedCommand `
        {
            aws ecr put-lifecycle-policy `
                --repository-name $BookingRepository `
                --lifecycle-policy-text "file://$EcrLifecyclePolicy" `
                --region $AwsRegion `
                --profile $AwsProfile `
                *> $null
        } `
        "Failed to configure booking-service ECR lifecycle policy."

    Invoke-CheckedCommand `
        {
            aws ecr put-lifecycle-policy `
                --repository-name $AvailabilityRepository `
                --lifecycle-policy-text "file://$EcrLifecyclePolicy" `
                --region $AwsRegion `
                --profile $AwsProfile `
                *> $null
        } `
        "Failed to configure availability-service ECR lifecycle policy."
}

Write-Host ""
Write-Host "5. Get ECR repository URIs"

$BookingEcr = aws ecr describe-repositories `
    --repository-names $BookingRepository `
    --query "repositories[0].repositoryUri" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($BookingEcr)) {
    throw "Booking ECR repository was not found."
}

$AvailabilityEcr = aws ecr describe-repositories `
    --repository-names $AvailabilityRepository `
    --query "repositories[0].repositoryUri" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($AvailabilityEcr)) {
    throw "Availability ECR repository was not found."
}

Write-Host "Booking ECR:"
Write-Host $BookingEcr

Write-Host "Availability ECR:"
Write-Host $AvailabilityEcr

Write-Host ""
Write-Host "6. Authenticate Docker with ECR"

$Registry = $BookingEcr.Split("/")[0]

$Password = aws ecr get-login-password `
    --region $AwsRegion `
    --profile $AwsProfile

if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($Password)) {
    throw "Failed to get ECR login password."
}

$Password |
docker login `
    --username AWS `
    --password-stdin $Registry

if ($LASTEXITCODE -ne 0) {
    throw "Docker ECR authentication failed."
}

Write-Host ""
Write-Host "7. Build application images"

Invoke-CheckedCommand `
    {
        docker compose build booking-service availability-service
    } `
    "Docker build failed."

Write-Host ""
Write-Host "8. Tag application images"

Invoke-CheckedCommand `
    {
        docker tag `
            barber-booking-service:8.4 `
            "${BookingEcr}:$ImageTag"
    } `
    "Failed to tag booking-service image."

Invoke-CheckedCommand `
    {
        docker tag `
            barber-availability-service:8.4 `
            "${AvailabilityEcr}:$ImageTag"
    } `
    "Failed to tag availability-service image."

Write-Host ""
Write-Host "9. Push application images"

Invoke-CheckedCommand `
    {
        docker push "${BookingEcr}:$ImageTag"
    } `
    "Failed to push booking-service image."

Invoke-CheckedCommand `
    {
        docker push "${AvailabilityEcr}:$ImageTag"
    } `
    "Failed to push availability-service image."

Write-Host ""
Write-Host "10. Verify ECR images"

Invoke-CheckedCommand `
    {
        aws ecr describe-images `
            --repository-name $BookingRepository `
            --image-ids "imageTag=$ImageTag" `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    } `
    "Booking image $ImageTag was not found in ECR."

Invoke-CheckedCommand `
    {
        aws ecr describe-images `
            --repository-name $AvailabilityRepository `
            --image-ids "imageTag=$ImageTag" `
            --region $AwsRegion `
            --profile $AwsProfile `
            *> $null
    } `
    "Availability image $ImageTag was not found in ECR."

Write-Host ""
Write-Host "11. EKS"

if (-not (Test-StackExists $EksStack)) {

    Write-Host "Deploying $EksStack..."

    Invoke-CheckedCommand `
        {
            aws cloudformation deploy `
                --stack-name $EksStack `
                --template-file $EksTemplate `
                --capabilities CAPABILITY_NAMED_IAM `
                --parameter-overrides `
                    PrivateSubnet1Id=$PrivateSubnet1Id `
                    PrivateSubnet2Id=$PrivateSubnet2Id `
                --region $AwsRegion `
                --profile $AwsProfile `
                --no-fail-on-empty-changeset
        } `
        "Failed to deploy $EksStack."
}
else {
    Write-Host "$EksStack already exists."
}

Write-Host ""
Write-Host "12. Update kubeconfig"

Invoke-CheckedCommand `
    {
        aws eks update-kubeconfig `
            --name $ClusterName `
            --region $AwsRegion `
            --profile $AwsProfile
    } `
    "Failed to update kubeconfig."

Invoke-CheckedCommand `
    {
        kubectl get nodes
    } `
    "Unable to communicate with EKS."

Write-Host ""
Write-Host "13. EKS Pod Identity Agent"

$AddonExitCode = Invoke-AwsSafe {
    aws eks describe-addon `
        --cluster-name $ClusterName `
        --addon-name eks-pod-identity-agent `
        --region $AwsRegion `
        --profile $AwsProfile `
        *> $null
}

if ($AddonExitCode -ne 0) {

    Invoke-CheckedCommand `
        {
            aws eks create-addon `
                --cluster-name $ClusterName `
                --addon-name eks-pod-identity-agent `
                --region $AwsRegion `
                --profile $AwsProfile
        } `
        "Failed to create EKS Pod Identity addon."
}
else {
    Write-Host "Pod Identity addon already exists."
}

Write-Host "Waiting for Pod Identity addon..."

Invoke-CheckedCommand `
    {
        aws eks wait addon-active `
            --cluster-name $ClusterName `
            --addon-name eks-pod-identity-agent `
            --region $AwsRegion `
            --profile $AwsProfile
    } `
    "Pod Identity addon did not become active."

Write-Host ""
Write-Host "14. AWS application resources"

Deploy-Stack `
    -StackName $ApplicationStack `
    -TemplateFile $ApplicationTemplate `
    -Iam

Write-Host ""
Write-Host "15. Read SNS topic ARNs"

$BookingEventsTopicArn = Get-StackOutput `
    -StackName $ApplicationStack `
    -OutputKey "BookingEventsTopicArn"

$AvailabilityEventsTopicArn = Get-StackOutput `
    -StackName $ApplicationStack `
    -OutputKey "AvailabilityEventsTopicArn"

Write-Host ""
Write-Host "16. Kubernetes service account"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "service-account.yaml")
    } `
    "Failed to apply service account."

Write-Host ""
Write-Host "17. Kubernetes ConfigMap"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "configmap.yaml")
    } `
    "Failed to apply ConfigMap."

Write-Host ""
Write-Host "18. Patch SNS ARNs"

$ConfigMapPatch = @{
    data = @{
        BOOKING_EVENTS_TOPIC_ARN = $BookingEventsTopicArn
        AVAILABILITY_EVENTS_TOPIC_ARN = $AvailabilityEventsTopicArn
    }
} | ConvertTo-Json -Depth 5

$PatchFile = Join-Path `
    $env:TEMP `
    ("barber-configmap-" + [guid]::NewGuid().ToString("N") + ".json")

$ConfigMapPatch |
Set-Content `
    -Path $PatchFile `
    -Encoding ascii

Invoke-CheckedCommand `
    {
        kubectl patch configmap barber-saas-aws-config `
            --type merge `
            --patch-file $PatchFile
    } `
    "Failed to patch ConfigMap."

Remove-Item `
    $PatchFile `
    -Force `
    -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "19. PostgreSQL secret"

$ExistingSecret = kubectl get secret barber-saas-db-secret `
    --ignore-not-found `
    -o name

if ([string]::IsNullOrWhiteSpace($ExistingSecret)) {

    $DbPassword =
        "BarberDev-" +
        [guid]::NewGuid().ToString("N").Substring(0, 16)

    Invoke-CheckedCommand `
        {
            kubectl create secret generic barber-saas-db-secret `
                --from-literal=DB_USERNAME=postgres `
                --from-literal=DB_PASSWORD=$DbPassword
        } `
        "Failed to create PostgreSQL secret."

    Write-Host "Database secret created."
}
else {
    Write-Host "Database secret already exists."
}

Write-Host ""
Write-Host "20. PostgreSQL"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "postgres.yaml")
    } `
    "Failed to deploy PostgreSQL."

Invoke-CheckedCommand `
    {
        kubectl rollout status deployment/postgres `
            --timeout=180s
    } `
    "PostgreSQL rollout failed."

Write-Host ""
Write-Host "21. Kafka"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "kafka.yaml")
    } `
    "Failed to deploy Kafka."

Invoke-CheckedCommand `
    {
        kubectl rollout status deployment/kafka `
            --timeout=180s
    } `
    "Kafka rollout failed."

Write-Host ""
Write-Host "22. Kafka topic initialization"

kubectl delete job kafka-init `
    --ignore-not-found=true `
    *> $null

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "kafka-init-job.yaml")
    } `
    "Failed to create Kafka init job."

Invoke-CheckedCommand `
    {
        kubectl wait `
            --for=condition=complete `
            job/kafka-init `
            --timeout=180s
    } `
    "Kafka initialization failed."

Write-Host ""
Write-Host "23. Deploy booking-service"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "booking-deployment.yaml")
    } `
    "Failed to deploy booking-service."

Invoke-CheckedCommand `
    {
        kubectl set image deployment/booking-service `
            booking-service="${BookingEcr}:$ImageTag"
    } `
    "Failed to set booking-service image."

Write-Host ""
Write-Host "24. Deploy availability-service"

Invoke-CheckedCommand `
    {
        kubectl apply -f `
            (Join-Path $KubernetesAws "availability-deployment.yaml")
    } `
    "Failed to deploy availability-service."

Invoke-CheckedCommand `
    {
        kubectl set image deployment/availability-service `
            availability-service="${AvailabilityEcr}:$ImageTag"
    } `
    "Failed to set availability-service image."

Write-Host ""
Write-Host "25. Wait for application services"

Invoke-CheckedCommand `
    {
        kubectl rollout status deployment/booking-service `
            --timeout=240s
    } `
    "booking-service rollout failed."

Invoke-CheckedCommand `
    {
        kubectl rollout status deployment/availability-service `
            --timeout=240s
    } `
    "availability-service rollout failed."

Write-Host ""
Write-Host "26. Verify deployed images"

kubectl get deployment booking-service availability-service `
    -o custom-columns=NAME:.metadata.name,IMAGE:.spec.template.spec.containers[0].image

Write-Host ""
Write-Host "27. Verify pods"

kubectl get pods

Write-Host ""
Write-Host "28. Verify services"

kubectl get services

Write-Host ""
Write-Host "DEV environment is ready."
Write-Host "Image tag: $ImageTag"