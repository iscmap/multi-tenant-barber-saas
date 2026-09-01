$bytes = New-Object byte[] 48

$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()

try {
    $rng.GetBytes($bytes)
}
finally {
    $rng.Dispose()
}

[Convert]::ToBase64String($bytes)