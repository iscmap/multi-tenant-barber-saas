param(
    [string]$BookingBaseUrl = "http://localhost:8081",
    [string]$AvailabilityBaseUrl = "http://localhost:8082"
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    throw "JWT_SECRET is not configured in the current PowerShell session."
}

function ConvertTo-Base64Url([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).
        TrimEnd('=').
        Replace('+', '-').
        Replace('/', '_')
}

function New-TestJwt(
    [string]$Subject,
    [string]$Scope,
    [string[]]$Roles,
    [int]$ExpiresInSeconds = 3600,
    [string]$Secret = $env:JWT_SECRET
) {
    $now = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
    $exp = $now + $ExpiresInSeconds

    $header = @{
        alg = "HS256"
        typ = "JWT"
    } | ConvertTo-Json -Compress

    $payload = @{
        sub   = $Subject
        iat   = $now
        exp   = $exp
        scope = $Scope
        roles = $Roles
    } | ConvertTo-Json -Compress

    $encodedHeader =
        ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($header))

    $encodedPayload =
        ConvertTo-Base64Url ([Text.Encoding]::UTF8.GetBytes($payload))

    $unsignedToken = "$encodedHeader.$encodedPayload"

    $hmac =
        [System.Security.Cryptography.HMACSHA256]::new(
            [Text.Encoding]::UTF8.GetBytes($Secret)
        )

    try {
        $signature =
            ConvertTo-Base64Url (
                $hmac.ComputeHash(
                    [Text.Encoding]::UTF8.GetBytes($unsignedToken)
                )
            )
    }
    finally {
        $hmac.Dispose()
    }

    return "$unsignedToken.$signature"
}

function Invoke-SecurityCheck(
    [string]$Name,
    [string]$Method,
    [string]$Url,
    [int]$ExpectedStatus,
    [string[]]$Headers = @(),
    [string]$Body = $null
) {
    $headerFile = [System.IO.Path]::GetTempFileName()
    $bodyFile = [System.IO.Path]::GetTempFileName()

    try {
        $arguments = @(
            "--silent",
            "--show-error",
            "--output", $bodyFile,
            "--dump-header", $headerFile,
            "--request", $Method,
            $Url
        )

        foreach ($header in $Headers) {
            $arguments += @("--header", $header)
        }

        if (-not [string]::IsNullOrWhiteSpace($Body)) {
            $arguments += @("--data", $Body)
        }

        & curl.exe @arguments

        if ($LASTEXITCODE -ne 0) {
            throw "curl failed for test: $Name"
        }

        $statusLine = Get-Content $headerFile |
            Where-Object { $_ -match "^HTTP/" } |
            Select-Object -Last 1

        if ($statusLine -notmatch "HTTP/\S+\s+(\d{3})") {
            throw "Could not determine HTTP status for: $Name"
        }

        $actualStatus = [int]$Matches[1]

        if ($actualStatus -ne $ExpectedStatus) {
            Write-Host "[FAILED] $Name - expected $ExpectedStatus, got $actualStatus"

            Write-Host "--- Headers ---"
            Get-Content $headerFile

            Write-Host "--- Body ---"
            Get-Content $bodyFile

            throw "Security validation failed."
        }

        Write-Host "[PASSED] $Name - HTTP $actualStatus"
    }
    finally {
        Remove-Item $headerFile -ErrorAction SilentlyContinue
        Remove-Item $bodyFile -ErrorAction SilentlyContinue
    }
}

$customerToken =
    New-TestJwt `
        -Subject "customer-1" `
        -Scope "bookings.read bookings.write availability.read" `
        -Roles @("CUSTOMER")

$serviceToken =
    New-TestJwt `
        -Subject "availability-service" `
        -Scope "internal" `
        -Roles @("SERVICE")

$wrongScopeToken =
    New-TestJwt `
        -Subject "customer-1" `
        -Scope "availability.read" `
        -Roles @("CUSTOMER")

$expiredToken =
    New-TestJwt `
        -Subject "customer-1" `
        -Scope "bookings.read" `
        -Roles @("CUSTOMER") `
        -ExpiresInSeconds -60

$oldSecretToken =
    New-TestJwt `
        -Subject "customer-1" `
        -Scope "bookings.read" `
        -Roles @("CUSTOMER") `
        -Secret "local-dev-jwt-secret-012345678901234567890123456789"

Write-Host ""
Write-Host "Running Barber SaaS security validation..."
Write-Host ""

Invoke-SecurityCheck `
    -Name "Booking health is public" `
    -Method "GET" `
    -Url "$BookingBaseUrl/actuator/health" `
    -ExpectedStatus 200

Invoke-SecurityCheck `
    -Name "Availability health is public" `
    -Method "GET" `
    -Url "$AvailabilityBaseUrl/actuator/health" `
    -ExpectedStatus 200

Invoke-SecurityCheck `
    -Name "Booking API requires authentication" `
    -Method "GET" `
    -Url "$BookingBaseUrl/api/v1/bookings/missing-security-test" `
    -ExpectedStatus 401

Invoke-SecurityCheck `
    -Name "Internal API requires authentication" `
    -Method "POST" `
    -Url "$BookingBaseUrl/api/v1/internal/bookings/reject-timeouts" `
    -ExpectedStatus 401

Invoke-SecurityCheck `
    -Name "Customer cannot access internal API" `
    -Method "POST" `
    -Url "$BookingBaseUrl/api/v1/internal/bookings/reject-timeouts" `
    -ExpectedStatus 403 `
    -Headers @(
        "Authorization: Bearer $customerToken"
    )

Invoke-SecurityCheck `
    -Name "Service can access internal API" `
    -Method "POST" `
    -Url "$BookingBaseUrl/api/v1/internal/bookings/reject-timeouts" `
    -ExpectedStatus 200 `
    -Headers @(
        "Authorization: Bearer $serviceToken",
        "X-Correlation-Id: security-125-service"
    )

Invoke-SecurityCheck `
    -Name "Wrong scope is forbidden" `
    -Method "POST" `
    -Url "$BookingBaseUrl/api/v1/bookings" `
    -ExpectedStatus 403 `
    -Headers @(
        "Authorization: Bearer $wrongScopeToken",
        "Idempotency-Key: security-125-wrong-scope",
        "Content-Type: application/json"
    ) `
    -Body '{
        "shopId":"shop-1",
        "barberId":"barber-1",
        "customerId":"customer-1",
        "date":"2026-09-01",
        "startTime":"10:00:00",
        "durationMinutes":30,
        "serviceCode":"HAIRCUT"
    }'

Invoke-SecurityCheck `
    -Name "Expired JWT is rejected" `
    -Method "GET" `
    -Url "$BookingBaseUrl/api/v1/bookings/missing-security-test" `
    -ExpectedStatus 401 `
    -Headers @(
        "Authorization: Bearer $expiredToken"
    )

Invoke-SecurityCheck `
    -Name "Old known JWT secret is rejected" `
    -Method "GET" `
    -Url "$BookingBaseUrl/api/v1/bookings/missing-security-test" `
    -ExpectedStatus 401 `
    -Headers @(
        "Authorization: Bearer $oldSecretToken"
    )

Invoke-SecurityCheck `
    -Name "Invalid booking input is rejected" `
    -Method "POST" `
    -Url "$BookingBaseUrl/api/v1/bookings" `
    -ExpectedStatus 400 `
    -Headers @(
        "Authorization: Bearer $customerToken",
        "X-Correlation-Id: security-125-validation",
        "Idempotency-Key: security-125-validation",
        "Content-Type: application/json"
    ) `
    -Body '{
        "shopId":"../../../etc/passwd",
        "barberId":"barber-1",
        "customerId":"customer-1",
        "date":"2026-09-01",
        "startTime":"10:00:00",
        "durationMinutes":30,
        "serviceCode":"HAIRCUT"
    }'

Invoke-SecurityCheck `
    -Name "Invalid availability input is rejected" `
    -Method "GET" `
    -Url "$AvailabilityBaseUrl/api/v1/availability/validate/shop-1/barber-1/2026-09-01/10:00/9999" `
    -ExpectedStatus 400 `
    -Headers @(
        "Authorization: Bearer $customerToken",
        "X-Correlation-Id: security-125-availability"
    )

Invoke-SecurityCheck `
    -Name "Booking Prometheus endpoint is public" `
    -Method "GET" `
    -Url "$BookingBaseUrl/actuator/prometheus" `
    -ExpectedStatus 200

Invoke-SecurityCheck `
    -Name "Availability Prometheus endpoint is public" `
    -Method "GET" `
    -Url "$AvailabilityBaseUrl/actuator/prometheus" `
    -ExpectedStatus 200

Write-Host ""
Write-Host "Security validation completed successfully."