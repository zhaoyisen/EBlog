param (
    [string]$BaseUrl = "http://localhost:3000"
)

$ErrorActionPreference = "Stop"
$script:failed = $false

function Write-Result {
    param (
        [string]$Step,
        [bool]$Success,
        [string]$Message
    )
    if ($Success) {
        Write-Host "[PASS] $Step : $Message" -ForegroundColor Green
    } else {
        Write-Host "[FAIL] $Step : $Message" -ForegroundColor Red
        $script:failed = $true
    }
}

Write-Host "Starting Auth Verification on $BaseUrl" -ForegroundColor Cyan
Write-Host "------------------------------------------"

try {
    # 1. GET /api/v1/auth/csrf
    $step = "1. GET /api/v1/auth/csrf"
    $resp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/csrf" -SessionVariable sess -Method Get
    $csrfData = $resp.Content | ConvertFrom-Json
    $token = $csrfData.data.token
    
    $hasCsrfCookie = $false
    try {
        $uri = New-Object System.Uri($BaseUrl)
        foreach ($cookie in $sess.Cookies.GetCookies($uri)) {
            if ($cookie.Name -eq "XSRF-TOKEN") {
                $hasCsrfCookie = $true
                break
            }
        }
    } catch {
        # Fallback for older PS or unexpected session state
    }

    if ($resp.StatusCode -eq 200 -and $token -and $hasCsrfCookie) {
        Write-Result $step $true "Status 200, Token obtained, XSRF-TOKEN cookie set."
    } else {
        Write-Result $step $false "Status: $($resp.StatusCode), Token: $token, Cookie: $hasCsrfCookie"
    }

    # 2. POST /api/v1/auth/login (No CSRF Header)
    $step = "2. POST /api/v1/auth/login (No CSRF)"
    $loginBody = @{
        email = "test@example.com"
        password = "wrong"
    } | ConvertTo-Json
    
    $resp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/login" -WebSession $sess -Method Post -ContentType "application/json" -Body $loginBody -SkipHttpErrorCheck
    if ($resp.StatusCode -eq 403) {
        Write-Result $step $true "Status 403 (Forbidden) as expected."
    } else {
        Write-Result $step $false "Expected 403, but got $($resp.StatusCode)."
    }

    # 3. POST /api/v1/auth/login (With CSRF Header)
    $step = "3. POST /api/v1/auth/login (With CSRF)"
    $headers = @{
        "X-XSRF-TOKEN" = $token
    }
    $resp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/login" -WebSession $sess -Method Post -ContentType "application/json" -Headers $headers -Body $loginBody -SkipHttpErrorCheck
    if ($resp.StatusCode -ne 403) {
        Write-Result $step $true "Status $($resp.StatusCode) (Not 403) as expected."
    } else {
        Write-Result $step $false "Expected not 403, but got 403."
    }

    # 4. POST /api/v1/auth/refresh (No CSRF)
    $step = "4. POST /api/v1/auth/refresh (No CSRF)"
    $resp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/refresh" -WebSession $sess -Method Post -SkipHttpErrorCheck
    if ($resp.StatusCode -ne 403) {
        Write-Result $step $true "Status $($resp.StatusCode) (Not 403) as expected."
    } else {
        Write-Result $step $false "Expected not 403, but got 403."
    }

    # 5. POST /api/v1/auth/logout (No CSRF)
    $step = "5. POST /api/v1/auth/logout (No CSRF)"
    $resp = Invoke-WebRequest -Uri "$BaseUrl/api/v1/auth/logout" -WebSession $sess -Method Post -SkipHttpErrorCheck
    
    $clearsRefreshToken = $false
    if ($resp.Headers.ContainsKey("Set-Cookie")) {
        $setCookies = $resp.Headers["Set-Cookie"]
        if ($setCookies -is [string]) {
            $setCookies = @($setCookies)
        }
        foreach ($c in $setCookies) {
            if ($c -match "refresh_token=;") {
                $clearsRefreshToken = $true
                break
            }
        }
    }

    if ($resp.StatusCode -eq 200) {
        $msg = "Status 200."
        if ($clearsRefreshToken) {
            $msg += " refresh_token cookie cleared."
        } else {
            $msg += " (Note: refresh_token cookie clear header not found in this response, might be expected if not logged in)"
        }
        Write-Result $step $true $msg
    } else {
        Write-Result $step $false "Expected 200, but got $($resp.StatusCode)."
    }

} catch {
    Write-Host "An error occurred: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

Write-Host "------------------------------------------"
Write-Host "Verification Complete." -ForegroundColor Cyan

if ($script:failed) {
    exit 2
} else {
    exit 0
}
