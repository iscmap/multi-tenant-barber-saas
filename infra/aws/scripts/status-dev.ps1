param(
    [string]$AwsProfile = "barber-dev",
    [string]$AwsRegion = "us-east-1"
)

$ErrorActionPreference = "Stop"

$NetworkStack = "barber-saas-dev-network"
$EcrStack = "barber-saas-dev-ecr"
$EksStack = "barber-saas-dev-eks"
$ApplicationStack = "barber-saas-dev-application"
$ClusterName = "barber-saas-dev"

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

function Get-StackStatus {
    param([string]$StackName)

    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    $Status = aws cloudformation describe-stacks `
        --stack-name $StackName `
        --query "Stacks[0].StackStatus" `
        --output text `
        --region $AwsRegion `
        --profile $AwsProfile `
        2>$null

    $ExitCode = $LASTEXITCODE

    $ErrorActionPreference = $PreviousErrorActionPreference

    if ($ExitCode -eq 0) {
        return $Status
    }

    return "NOT_FOUND"
}

Write-Host ""
Write-Host "=== Barber SaaS DEV Status ==="
Write-Host ""

$env:AWS_PROFILE = $AwsProfile
$env:AWS_REGION = $AwsRegion

aws sts get-caller-identity `
    --profile $AwsProfile `
    *> $null

if ($LASTEXITCODE -ne 0) {
    throw "AWS authentication failed."
}

Write-Host "CloudFormation"
Write-Host "--------------"

Write-Host "$NetworkStack      : $(Get-StackStatus $NetworkStack)"
Write-Host "$EcrStack          : $(Get-StackStatus $EcrStack)"
Write-Host "$EksStack          : $(Get-StackStatus $EksStack)"
Write-Host "$ApplicationStack  : $(Get-StackStatus $ApplicationStack)"

Write-Host ""
Write-Host "EKS"
Write-Host "---"

$PreviousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

$ClusterStatus = aws eks describe-cluster `
    --name $ClusterName `
    --query "cluster.status" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile `
    2>$null

$ClusterExitCode = $LASTEXITCODE

$ErrorActionPreference = $PreviousErrorActionPreference

if ($ClusterExitCode -eq 0) {

    Write-Host "Cluster: $ClusterName"
    Write-Host "Status : $ClusterStatus"

    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    $NodeGroups = aws eks list-nodegroups `
        --cluster-name $ClusterName `
        --query "nodegroups[]" `
        --output text `
        --region $AwsRegion `
        --profile $AwsProfile `
        2>$null

    $NodeGroupExitCode = $LASTEXITCODE

    $ErrorActionPreference = $PreviousErrorActionPreference

    if ($NodeGroupExitCode -eq 0 -and $NodeGroups) {
        Write-Host "Node groups: $NodeGroups"
    }
    else {
        Write-Host "Node groups: none"
    }

    aws eks update-kubeconfig `
        --name $ClusterName `
        --region $AwsRegion `
        --profile $AwsProfile `
        *> $null

    if ($LASTEXITCODE -eq 0) {

        Write-Host ""
        Write-Host "Kubernetes"
        Write-Host "----------"

        kubectl get nodes
        kubectl get pods
    }
}
else {
    Write-Host "Cluster: NOT_FOUND"
}

Write-Host ""
Write-Host "EC2 instances"
Write-Host "-------------"

aws ec2 describe-instances `
    --filters `
        "Name=instance-state-name,Values=pending,running,stopping,stopped" `
    --query "Reservations[].Instances[].[InstanceId,InstanceType,State.Name,PrivateIpAddress,Tags[?Key=='Name']|[0].Value]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "NAT Gateways"
Write-Host "------------"

aws ec2 describe-nat-gateways `
    --filter "Name=state,Values=pending,available,deleting" `
    --query "NatGateways[].[NatGatewayId,State,VpcId,SubnetId,NatGatewayAddresses[0].PublicIp]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "Public IPv4 / Elastic IP"
Write-Host "------------------------"

aws ec2 describe-addresses `
    --query "Addresses[].[AllocationId,PublicIp,AssociationId,NetworkInterfaceId]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "EBS volumes"
Write-Host "-----------"

aws ec2 describe-volumes `
    --filters "Name=status,Values=creating,available,in-use" `
    --query "Volumes[].[VolumeId,Size,VolumeType,State,Attachments[0].InstanceId]" `
    --output table `
    --region $AwsRegion `
    --profile $AwsProfile

Write-Host ""
Write-Host "ECR"
Write-Host "---"

$Repositories = @(
    "barber-saas/dev/booking-service",
    "barber-saas/dev/availability-service"
)

foreach ($Repository in $Repositories) {

    $PreviousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"

    $RepositoryUri = aws ecr describe-repositories `
        --repository-names $Repository `
        --query "repositories[0].repositoryUri" `
        --output text `
        --region $AwsRegion `
        --profile $AwsProfile `
        2>$null

    $RepositoryExitCode = $LASTEXITCODE

    $ErrorActionPreference = $PreviousErrorActionPreference

    if ($RepositoryExitCode -eq 0) {

        $PreviousErrorActionPreference = $ErrorActionPreference
        $ErrorActionPreference = "Continue"

        $ImageCount = aws ecr list-images `
            --repository-name $Repository `
            --query "length(imageIds)" `
            --output text `
            --region $AwsRegion `
            --profile $AwsProfile `
            2>$null

        $ImageCountExitCode = $LASTEXITCODE

        $ErrorActionPreference = $PreviousErrorActionPreference

        Write-Host $RepositoryUri

        if ($ImageCountExitCode -eq 0) {
            Write-Host "Images: $ImageCount"
        }
        else {
            Write-Host "Images: UNKNOWN"
        }
    }
    else {
        Write-Host "$Repository : NOT_FOUND"
    }
}

Write-Host ""
Write-Host "Application resources"
Write-Host "---------------------"

if ((Get-StackStatus $ApplicationStack) -ne "NOT_FOUND") {

    aws cloudformation describe-stack-resources `
        --stack-name $ApplicationStack `
        --query "StackResources[].[ResourceType,PhysicalResourceId,ResourceStatus]" `
        --output table `
        --region $AwsRegion `
        --profile $AwsProfile
}
else {
    Write-Host "Application stack not found."
}

Write-Host ""
Write-Host "Potential billable runtime resources"
Write-Host "------------------------------------"

$PreviousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

$RunningInstances = aws ec2 describe-instances `
    --filters "Name=instance-state-name,Values=pending,running" `
    --query "length(Reservations[].Instances[])" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile `
    2>$null

$RunningInstancesExitCode = $LASTEXITCODE

$ErrorActionPreference = $PreviousErrorActionPreference

if ($RunningInstancesExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($RunningInstances)) {
    $RunningInstances = "UNKNOWN"
}

$PreviousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

$NatGatewayCount = aws ec2 describe-nat-gateways `
    --filter "Name=state,Values=pending,available,deleting" `
    --query "length(NatGateways)" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile `
    2>$null

$NatExitCode = $LASTEXITCODE

$ErrorActionPreference = $PreviousErrorActionPreference

if ($NatExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($NatGatewayCount)) {
    $NatGatewayCount = "UNKNOWN"
}

$PreviousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

$ElasticIpCount = aws ec2 describe-addresses `
    --query "length(Addresses)" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile `
    2>$null

$ElasticIpExitCode = $LASTEXITCODE

$ErrorActionPreference = $PreviousErrorActionPreference

if ($ElasticIpExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($ElasticIpCount)) {
    $ElasticIpCount = "UNKNOWN"
}

$PreviousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

$EbsVolumeCount = aws ec2 describe-volumes `
    --filters "Name=status,Values=available,in-use" `
    --query "length(Volumes)" `
    --output text `
    --region $AwsRegion `
    --profile $AwsProfile `
    2>$null

$EbsExitCode = $LASTEXITCODE

$ErrorActionPreference = $PreviousErrorActionPreference

if ($EbsExitCode -ne 0 -or [string]::IsNullOrWhiteSpace($EbsVolumeCount)) {
    $EbsVolumeCount = "UNKNOWN"
}

Write-Host "EKS cluster       : $(if ($ClusterExitCode -eq 0) { $ClusterStatus } else { 'NOT_FOUND' })"
Write-Host "Running EC2       : $RunningInstances"
Write-Host "NAT gateways      : $NatGatewayCount"
Write-Host "Elastic IPs       : $ElasticIpCount"
Write-Host "EBS volumes       : $EbsVolumeCount"
Write-Host "ECR stack         : $(Get-StackStatus $EcrStack)"

Write-Host ""
Write-Host "Status complete."
