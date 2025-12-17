@echo off
title Git手动提交上传
color 0A

echo.
echo 正在检查Git状态...
echo ========================================
git status
echo ========================================
echo.

set /p add_choice=是否添加所有更改？(y/n): 
if /i "%add_choice%"=="y" (
    echo 添加所有更改...
    git add .
) else (
    echo 跳过添加
)

:commit_input
echo.
set /p commit_msg=请输入提交信息: 
if "%commit_msg%"=="" (
    echo 错误：提交信息不能为空！
    goto commit_input
)

echo 提交更改...
git commit -m "%commit_msg%"
if errorlevel 1 (
    echo 提交失败，可能没有更改
    echo 是否继续推送？(y/n)
    set /p continue_choice=
    if /i not "%continue_choice%"=="y" (
        echo 操作取消
        pause
        exit
    )
)

echo.
echo 拉取远程更新...
git pull origin main
if errorlevel 1 (
    echo 拉取失败，尝试强制推送
    git push -f origin main
) else (
    echo 推送更改...
    git push origin main
)

echo.
echo 操作完成！
pause