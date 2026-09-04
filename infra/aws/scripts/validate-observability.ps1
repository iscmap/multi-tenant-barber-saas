param(
    [string]$AwsProfile = "barber-dev",
    [string]$AwsRegion = "us-east-1"
)

$ErrorActionPreference = "Stop"

$ClusterName = "barber-saas-dev"
$EksStack = "barber-saas-dev-eks"

$CloudWatchAddonName = "amazon-cloudwatch-observability"
$PodIdentityAddonName = "eks-pod-identity-agent"

$DashboardName = "barber-saas-dev-operations"

$ExpectedAlarms = @(
    "barber-saas-dev-high-node-cpu",
    "barber-saas-dev-high-node-memory",
    "barber-saas-dev-failed-node"
)

$ExpectedLogGroups = @(
    "/aws/containerinsights/$ClusterName/application",
    "/aws/containerinsights/$ClusterName/host",
    "/aws/containerinsights/$ClusterName/dataplane"
)

$Failed = $false


function Write-CheckPassed {
    param([string]$Message)

    Write-Host "[PASS] $Message"
}


function Write-CheckFailed {
    param([string]$Message)

    $script:Failed = $true
    Write-Host "[FAIL] $Message"
}


function Write-CheckInfo {
    param([string]$Message)

    Write-Host "[INFO] $Message"
}


Write-Host ""
Write-Host "=== Barber SaaS Observability Validation ==="
Write-Host ""


Write-Host "1. AWS authentication"

aws sts get-caller-identity `
    --profile $AwsProfile `
    --region $AwsRegion `
    *> $null

if ($LASTEXITCODE -eq 0) {
    Write-CheckPassed "AWS authentication"
}
else {
    Write-CheckFailed "AWS authentication"
    exit 1
}


Write-Host ""
Write-Host "2. EKS cluster"

$ClusterStatus = aws eks describe-cluster `
    --name $ClusterName `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "cluster.status" `
    --output text

if ($LASTEXITCODE -eq 0 -and $ClusterStatus -eq "ACTIVE") {
    Write-CheckPassed "EKS cluster is ACTIVE"
}
else {
    Write-CheckFailed "EKS cluster is not ACTIVE"
}


Write-Host ""
Write-Host "3. EKS Pod Identity add-on"

$PodIdentityStatus = aws eks describe-addon `
    --cluster-name $ClusterName `
    --addon-name $PodIdentityAddonName `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "addon.status" `
    --output text

if ($LASTEXITCODE -eq 0 -and $PodIdentityStatus -eq "ACTIVE") {
    Write-CheckPassed "Pod Identity add-on is ACTIVE"
}
else {
    Write-CheckFailed "Pod Identity add-on is not ACTIVE"
}


Write-Host ""
Write-Host "4. CloudWatch Observability add-on"

$CloudWatchAddonStatus = aws eks describe-addon `
    --cluster-name $ClusterName `
    --addon-name $CloudWatchAddonName `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "addon.status" `
    --output text

if (
    $LASTEXITCODE -eq 0 -and
    $CloudWatchAddonStatus -eq "ACTIVE"
) {
    Write-CheckPassed "CloudWatch Observability add-on is ACTIVE"
}
else {
    Write-CheckFailed "CloudWatch Observability add-on is not ACTIVE"
}


Write-Host ""
Write-Host "5. Kubernetes CloudWatch workloads"

$CloudWatchPods = kubectl get pods `
    -n amazon-cloudwatch `
    --no-headers `
    2>$null

if ($LASTEXITCODE -eq 0 -and $CloudWatchPods) {

    Write-CheckPassed "amazon-cloudwatch namespace contains workloads"

    kubectl get pods -n amazon-cloudwatch
}
else {
    Write-CheckFailed "CloudWatch Kubernetes workloads were not found"
}


Write-Host ""
Write-Host "6. CloudWatch dashboard"

$Dashboard = aws cloudwatch get-dashboard `
    --dashboard-name $DashboardName `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "DashboardName" `
    --output text

if (
    $LASTEXITCODE -eq 0 -and
    $Dashboard -eq $DashboardName
) {
    Write-CheckPassed "Dashboard exists: $DashboardName"
}
else {
    Write-CheckFailed "Dashboard was not found: $DashboardName"
}


Write-Host ""
Write-Host "7. CloudWatch alarms"

foreach ($AlarmName in $ExpectedAlarms) {

    $AlarmState = aws cloudwatch describe-alarms `
        --alarm-names $AlarmName `
        --profile $AwsProfile `
        --region $AwsRegion `
        --query "MetricAlarms[0].StateValue" `
        --output text

    if (
        $LASTEXITCODE -eq 0 -and
        -not [string]::IsNullOrWhiteSpace($AlarmState) -and
        $AlarmState -ne "None"
    ) {
        Write-CheckPassed "$AlarmName exists"
        Write-CheckInfo "$AlarmName state: $AlarmState"
    }
    else {
        Write-CheckFailed "$AlarmName was not found"
    }
}


Write-Host ""
Write-Host "8. SNS alert topic"

$TopicArn = aws cloudformation describe-stacks `
    --stack-name $EksStack `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "Stacks[0].Outputs[?OutputKey=='OperationsAlertTopicArn'].OutputValue | [0]" `
    --output text

if (
    $LASTEXITCODE -eq 0 -and
    -not [string]::IsNullOrWhiteSpace($TopicArn) -and
    $TopicArn -ne "None"
) {
    Write-CheckPassed "Operations SNS topic exists"
    Write-CheckInfo $TopicArn
}
else {
    Write-CheckFailed "Operations SNS topic was not found"
}


Write-Host ""
Write-Host "9. SNS subscriptions"

if (
    -not [string]::IsNullOrWhiteSpace($TopicArn) -and
    $TopicArn -ne "None"
) {

    $Subscriptions = aws sns list-subscriptions-by-topic `
        --topic-arn $TopicArn `
        --profile $AwsProfile `
        --region $AwsRegion `
        --query "Subscriptions" `
        --output json |
        ConvertFrom-Json

    if ($null -eq $Subscriptions -or $Subscriptions.Count -eq 0) {

        Write-CheckFailed "No SNS subscriptions were found"
    }
    else {

        $ConfirmedSubscriptions = @(
            $Subscriptions |
            Where-Object {
                $_.SubscriptionArn -ne "PendingConfirmation"
            }
        )

        if ($ConfirmedSubscriptions.Count -gt 0) {

            Write-CheckPassed "SNS has confirmed subscription(s)"

            foreach ($Subscription in $ConfirmedSubscriptions) {
                Write-CheckInfo "$($Subscription.Protocol): $($Subscription.Endpoint)"
            }
        }
        else {
            Write-CheckFailed "SNS subscription is still PendingConfirmation"
        }
    }
}


Write-Host ""
Write-Host "10. CloudWatch log groups"

foreach ($LogGroup in $ExpectedLogGroups) {

    $LogGroupJson = aws logs describe-log-groups `
        --log-group-name-prefix $LogGroup `
        --profile $AwsProfile `
        --region $AwsRegion `
        --query "logGroups[?logGroupName=='$LogGroup'] | [0]" `
        --output json

    $LogGroupInfo = $LogGroupJson | ConvertFrom-Json

    if ($null -eq $LogGroupInfo) {

        Write-CheckFailed "Missing log group: $LogGroup"
        continue
    }

    Write-CheckPassed "Log group exists: $LogGroup"

    if ($LogGroupInfo.retentionInDays -eq 7) {
        Write-CheckPassed "7-day retention: $LogGroup"
    }
    else {
        Write-CheckFailed "Expected 7-day retention: $LogGroup"
    }
}


Write-Host ""
Write-Host "11. Application logs"

$ApplicationLogGroup =
    "/aws/containerinsights/$ClusterName/application"

$RecentLogs = aws logs filter-log-events `
    --log-group-name $ApplicationLogGroup `
    --limit 10 `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "events[].message" `
    --output text

if (
    $LASTEXITCODE -eq 0 -and
    -not [string]::IsNullOrWhiteSpace($RecentLogs)
) {
    Write-CheckPassed "Application logs are reaching CloudWatch"
}
else {
    Write-CheckFailed "No CloudWatch application logs were found"
}


Write-Host ""
Write-Host "12. Runtime resources / cost review"

Write-Host ""
Write-Host "EKS cluster:"
Write-Host "------------"

aws eks describe-cluster `
    --name $ClusterName `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "cluster.[name,status]" `
    --output table


Write-Host ""
Write-Host "EC2 instances:"
Write-Host "--------------"

aws ec2 describe-instances `
    --filters "Name=instance-state-name,Values=pending,running" `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "Reservations[].Instances[].[InstanceId,InstanceType,State.Name]" `
    --output table


Write-Host ""
Write-Host "NAT gateways:"
Write-Host "-------------"

aws ec2 describe-nat-gateways `
    --filter "Name=state,Values=pending,available" `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "NatGateways[].[NatGatewayId,State]" `
    --output table


Write-Host ""
Write-Host "Elastic IP addresses:"
Write-Host "---------------------"

aws ec2 describe-addresses `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "Addresses[].[AllocationId,PublicIp,AssociationId]" `
    --output table


Write-Host ""
Write-Host "EBS volumes:"
Write-Host "------------"

aws ec2 describe-volumes `
    --filters "Name=status,Values=available,in-use" `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "Volumes[].[VolumeId,Size,VolumeType,State]" `
    --output table


Write-Host ""
Write-Host "ECR repositories:"
Write-Host "-----------------"

aws ecr describe-repositories `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "repositories[?contains(repositoryName, 'barber-saas')].repositoryName" `
    --output table


Write-Host ""
Write-Host "CloudWatch log groups:"
Write-Host "----------------------"

aws logs describe-log-groups `
    --log-group-name-prefix "/aws/containerinsights/$ClusterName" `
    --profile $AwsProfile `
    --region $AwsRegion `
    --query "logGroups[].[logGroupName,retentionInDays]" `
    --output table


Write-Host ""
Write-Host "13. Final result"

if ($Failed) {

    Write-Host ""
    Write-Host "Observability validation FAILED."
    exit 1
}

Write-Host ""
Write-Host "Observability validation PASSED."
Write-Host ""
Write-Host "Step 14.4 validation complete."