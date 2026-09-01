param(
    [string]$Subject = "customer-1",
    [string]$Scope = "bookings.read bookings.write availability.read",
    [string[]]$Roles = @("CUSTOMER"),
    [int]$ExpiresInSeconds = 3600
)

if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
    throw "JWT_SECRET is not configured."
}

function ConvertTo-Base64Url([byte[]]$bytes) {
    return [Convert]::ToBase64String($bytes).
        TrimEnd('=').
        Replace('+','-').
        Replace('/','_')
}

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

$data = "$encodedHeader.$encodedPayload"

$hmac =
    [System.Security.Cryptography.HMACSHA256]::new(
        [Text.Encoding]::UTF8.GetBytes($env:JWT_SECRET)
    )

try {
    $signature =
        ConvertTo-Base64Url (
            $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($data))
        )
}
finally {
    $hmac.Dispose()
}

$token = "$data.$signature"

Write-Output $token