@echo off
setlocal enabledelayedexpansion
set DOCKER_ADDR=default.registry.tke-syyx.com
set DOCKER_DIR=syyx-tpf
set PROJECT_NAME=tpf-java-base-8-test
set JDK_BASE_TAG=1.2
::设置镜像名 addr + dir + PROJECT_NAME +tag
set IMAGE_FULL_NAME=%DOCKER_ADDR%/%DOCKER_DIR%/%PROJECT_NAME%:%JDK_BASE_TAG%
echo IMAGE_FULL_NAME !IMAGE_FULL_NAME!
::设置自己docker私服的用户名和密码
set username=tkestack
set password=eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE5ODA3NDcwMzUsImp0aSI6IjdhYTciLCJpYXQiOjE2NjUzODcwMzUsInVzciI6ImppYW50dSIsInRlZCI6ImRlZmF1bHQifQ.anEJJzQS1XYisFfMc1uWNMTo-ZKu7SDQh9X49sTnggvc3rUb-tusibjxBa2oittAOsRbgeL8d_7UD85bxiNIBaaKgrthMplZaU3ZuaRdziDE34mTkOZ8dH54gtpenpRAr7q45QtXAQ6ClE9PQaIs1Y21wb88mk5gzVVqOklXA9X0ZeHtMwZf-cZz-8y3bZX_bEnT6pqLO6XIaMA1FahYhyE8Qs2SXiHrayyYaBae1WMVBPwI2SdF8_fkpqAKnVe0OYUHvyq7IGAOMevusGuzYzvUNVafexwCC8jaBsU21bIPMhJVlYVLnX1BFwdsnR-ToGNuIX9u19azK6dEPLVIkA
docker login -u !username! -p !password! !DOCKER_ADDR!
echo --------------------------build baseJdk begin-----------------------------------
docker build -t !IMAGE_FULL_NAME! .
echo --------------------------push baseJdk begin-----------------------------------
docker push !IMAGE_FULL_NAME!
echo --------------------------delete baseJdk begin-----------------------------------
docker rmi !IMAGE_FULL_NAME!
echo --------------------------build baseJdk finish-----------------------------------