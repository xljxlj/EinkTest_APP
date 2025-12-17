@echo off
echo 开始上传到GitHub...
echo.

REM 添加文件
git add .
echo 文件已添加

REM 提交
if "%1"=="" (
    git commit -m "Update: %date% %time%"
) else (
    git commit -m "%*"
)

REM 拉取并推送
git pull origin main
git push origin main

echo.
echo 上传完成！
pause