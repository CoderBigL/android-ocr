这是android部分代码
## 基本介绍

**此文档是**《[OCR文字识别实战教程-零基础](https://www.bilibili.com/video/BV1RK411b7DU/)，SpringBoot结合PaddleOCR》实现 车牌识别、文本识别、身份证识别 地址：[https://www.bilibili.com/video/BV1RK411b7DU/?vd_source=b4307343204f5c0271966f7fe276f0eb](https://www.bilibili.com/video/BV1RK411b7DU/?vd_source=b4307343204f5c0271966f7fe276f0eb)，**教程对应的部分文档，全部文档共89页，剩余文档和完整源代码 请关注B站视频**

### 效果演示
![image](https://github.com/CoderBigL/ocr-api/assets/29909236/26133601-4a3b-4b1e-b8fa-b2167c14c6b5)
上面的图片为Android APP 的界面，从左边开始，分别为主界面、拍照界面、识别及结果界面。
### 架构说明
OCR实战项目整体架构图如下：
![ocr实战架构图.jpg](https://cdn.nlark.com/yuque/0/2023/jpeg/13011155/1696646802468-b02332df-5f76-4549-8e37-cbc4f86b5ae2.jpeg#averageHue=%23f7e6ce&clientId=u636b264f-584e-4&from=drop&id=KweBU&originHeight=765&originWidth=1624&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=119499&status=done&style=stroke&taskId=udd041dbf-133c-4760-8f42-b0ed2aef5f8&title=)

- APP/WEB/小程序为OCR识别接口调用端，调用OCR接口，实现OCR功能。本项目我们只实现Android APP开发。
- Nginx反向代理和负载均衡功能，通过Nginx实现对外网暴露接口，对内负载均衡SpringBoot实现的OCR服务。
- OCR服务通过Springboot实现，主要功能是提供具体的OCR接口实现，其流程是调用内部PaddleOCR服务，解析和处理返回结果，最终返回结果给接口调用者。为了稳定性和安全性，添加了熔断限流、Token认证功能。为了方便部署，会以Docker形式部署该服务。
- PaddleOCR是OCR识别的具体实现，会提供一个OCR识别接口，供内部调用。由于不同的部署方式（普通部署和paddleocr serving方式部署），PaddleOCR在普通部署方式下，无法利用CPU多核（Servering方式不存在该问题），因此会在同一个服务器部署多个实例，解决CPU利用率差以提升性能。为了方便PaddleOCR部署，会以Docker形式部署。后边会讲解普通方式部署和Servering方式部署，如何构建docker镜像及部署流程。
### 主要技术栈

- 开发语言：java、python（不需要python基础）
- springboot 实现业务接口
- python flask 实现识别接口
- Sentinel限流熔断
- JWT Token 认证
- PaddlePaddle
- PaddleOCR 
- Nginx 反向代理和负载均衡
- Docker 镜像制作及部署服务
- Android 原生开发

本课程我们将借助PaddleOCR 和 PP-OPCRv4/3 实现文本识别、车牌识别、身份证识别。**本课程不涉及算法、模型训练等知识**，使用PaddleOCR提供的训练好的模型，没有晦涩难懂的技术，小白也能轻松入手。

**文档中的代码会和实际源代码有细微差别，以源代码为准（对实际学习不会有任何影响）**
## PaddleOCR介绍
PaddleOCR是一款由百度开发的OCR（光学字符识别）工具库。它旨在为开发者提供一套丰富、领先、且实用的OCR工具，以帮助他们训练出更好的模型并应用于实际场景。
PaddleOCR具有以下特点：

1. **超轻量模型：PaddleOCR采用了轻量级模型，以便在移动设备和嵌入式设备上运行。**
2. 通用识别大模型：除了轻量级模型外，PaddleOCR还提供了通用识别大模型，以适应更多的应用场景。
3. 算法丰富且开源：PaddleOCR集成了多种与OCR相关的前沿算法，并进行了开源，以便更多的开发者可以共享和使用。
4. 支持自定义训练：**开发者可以根据自己的需求，使用PaddleOCR提供的工具和框架自定义训练模型**。
5. 支持C++预测、**端侧部署、服务部署**：PaddleOCR不仅支持C++预测，还支持在端侧和服务上进行部署，具有很好的灵活性和可扩展性。
6. 行业特色模型：PaddleOCR开发了具有行业特色的模型PP-OCR和PP-Structure，并打通了数据生产、模型训练、压缩、预测部署的全流程。

总的来说，PaddleOCR是一款功能强大、实用便捷的OCR工具库，它提供了一系列前沿的算法和自定义训练的支持，旨在帮助开发者更好地应用OCR技术于各种实际场景中。
github:[https://github.com/PaddlePaddle/PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR)
### PaddleOCR应用场景
表单识别、票据识别、电表识别、车牌识别、身份证&银行卡、手写体识别、化验单识别 等等
### PP-OCRv4模型
PP-OCRv4提供一套通用的OCR识别模型，可以识别多语言的文字，在速度和精度上都达到了比较好的效果。**不指定模型版本，会默认下载最新的模型（PP-OCRv4）。**
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696660796752-e0e7fbfe-a7d2-4a4d-b723-5be3acdb1c54.png#averageHue=%23fdfbf9&clientId=u88108e0d-ebc3-4&from=paste&height=178&id=uc93ac964&originHeight=222&originWidth=1063&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=53332&status=done&style=stroke&taskId=u953e9495-ff56-4225-876a-c4dd72fd466&title=&width=850.4)
 ![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696660900341-518393e6-3b7f-4e93-b418-70025383bb3c.png#averageHue=%23e2c192&clientId=u88108e0d-ebc3-4&from=paste&height=351&id=u89ef8177&originHeight=439&originWidth=1086&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=90505&status=done&style=stroke&taskId=u41031e8f-3dd2-44a7-b74f-b3fb13323bf&title=&width=868.8)
具体参考[https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/doc/doc_ch/models_list.md](https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.7/doc/doc_ch/models_list.md)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696661594456-23365665-dd84-4987-bf8b-05931c1c50cb.png#averageHue=%23e1ba82&clientId=u88108e0d-ebc3-4&from=paste&height=265&id=u2bec5a40&originHeight=331&originWidth=1283&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=57989&status=done&style=stroke&taskId=ua44e41a4-0dc6-40a4-a271-8a070a32745&title=&width=1026.4)
使用时，我们只需要下载推理模型即可。
下载模型后，解压放到对应目录即可，windows为C:\Users\用户\.paddleocr\whl ，
linux为用户目录下\.paddleocr\whl
## PaddleOCR环境搭建
### windows下环境搭建
#### 安装python
下载安装包：[https://www.python.org/ftp/python/3.8.10/python-3.8.10-amd64.exe](https://www.python.org/ftp/python/3.8.10/python-3.8.10-amd64.exe)
**1.双击安装包进行安装**
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289511906-855c21a3-5239-4e86-8187-bb00b3cb62dc.png#averageHue=%23fdefef&clientId=uff2457c5-bf3f-4&from=paste&id=ud8d30d63&originHeight=98&originWidth=702&originalType=url&ratio=1&rotation=0&showTitle=false&size=11332&status=done&style=stroke&taskId=u60151448-d925-49bc-8ddf-68a6546c1c5&title=)
**2.勾上Install launcher for all users 和 Add Python 3.8 to PATH**
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512165-bcfc87d4-acae-485b-9c94-e9791c652f7c.png#averageHue=%23f9f3ee&clientId=uff2457c5-bf3f-4&from=paste&id=u2d86bbb6&originHeight=491&originWidth=829&originalType=url&ratio=1&rotation=0&showTitle=false&size=199823&status=done&style=stroke&taskId=u1fe0e841-0f36-4a9b-9526-31089e035a8&title=)
**3. 可以选择默认安装，也可以选择自定义安装。选择自定义安装如下：**
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512159-cd57d022-9ef8-464e-890d-5ef05383c82c.png#averageHue=%23f9f7f2&clientId=uff2457c5-bf3f-4&from=paste&id=ue7338570&originHeight=507&originWidth=830&originalType=url&ratio=1&rotation=0&showTitle=false&size=180486&status=done&style=stroke&taskId=uf60ecf95-3bcd-4ed4-b1a8-0d6be160333&title=)
保持默认，点击next 
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512188-a2ea4cc2-cd54-4eb6-882f-2d69d79c3a10.png#averageHue=%23f8f6f0&clientId=uff2457c5-bf3f-4&from=paste&id=u67c2cac7&originHeight=512&originWidth=832&originalType=url&ratio=1&rotation=0&showTitle=false&size=191716&status=done&style=stroke&taskId=u1db2ce40-c9d0-49aa-ba62-a6f800d231b&title=)
如上图，选择Install for all users. 最后 点击 Install 安装。
**4.验证**
在命令行中输入如下：
python -V
输出 Python 3.8.10 安装成功
#### 安装PaddlePaddle
打开：[https://www.paddlepaddle.org.cn/](https://www.paddlepaddle.org.cn/) ，选择 pip方式，安装比较简单方便。 选择如下（**CPU版本**），复制安装命令，安装：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512055-fd48beaa-29fb-4b90-aef0-ab0a7820b935.png#averageHue=%23f9f9f9&clientId=uff2457c5-bf3f-4&from=paste&id=ucf64e115&originHeight=452&originWidth=1017&originalType=url&ratio=1&rotation=0&showTitle=false&size=34708&status=done&style=stroke&taskId=u1a62d11c-6aa3-4c9a-98e2-65b6e6e4305&title=)
```shell
python -m pip install paddlepaddle==2.5.1 -i https://pypi.tuna.tsinghua.edu.cn/simple
```
**验证安装**
安装完成后，打开命令行，您可以使用输入 python 或 python3 进入 python 解释器，输入import paddle ，再输入paddle.utils.run_check()
如果出现PaddlePaddle is installed successfully!，说明您已成功安装。
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512181-cd97be54-d31f-4ee4-affa-df736371cc0f.png#averageHue=%23191919&clientId=uff2457c5-bf3f-4&from=paste&id=ub796322c&originHeight=167&originWidth=679&originalType=url&ratio=1&rotation=0&showTitle=false&size=11557&status=done&style=stroke&taskId=uc65a116d-086d-469e-bb7c-c1e3ccea7bb&title=)
#### 安装PaddleOCR
参考[https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.6/doc/doc_ch/quickstart.md](https://github.com/PaddlePaddle/PaddleOCR/blob/release/2.6/doc/doc_ch/quickstart.md)
执行如下命令：
```shell
pip install "paddleocr>=2.6.0" -i https://mirror.baidu.com/pypi/simple
```
如果安装时提示如下：
![提示截屏.jpg](https://cdn.nlark.com/yuque/0/2023/jpeg/13011155/1698225338230-8668e730-f5a8-45cd-ac4e-0339947726ad.jpeg#averageHue=%234e4837&clientId=uf6190bc4-de26-4&from=paste&height=1152&id=uba4e51b1&originHeight=1440&originWidth=2560&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=333288&status=done&style=stroke&taskId=u513aba00-bb52-4be6-8797-4fa15e72502&title=&width=2048)
根据提示我们需要把下面的路径加入到环境变量，否则可能paddocr命令无法执行
C:\Users\用户\AppData\Local\Programs\Python\Python38\Scripts

**验证**
1.输入 pip list ，看是否有paddleocr模块
2.**使用paddocr命令，进行ocr识别**。./imgs/11.jpg 替换成某个图片路径，windows下替换成windos风格的路径，例如”C:\文档资料\ocr\1.png“
```shell
#--use_angle_cls true设置使用方向分类器识别180度旋转文字，--use_gpu false设置不使用GPU 
paddleocr --image_dir ./imgs/11.jpg --use_angle_cls true --use_gpu false
```
例如输出如下：
# ![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1695289512369-b0c464b9-492a-4411-971b-0c70b36328c3.png#averageHue=%23221e1a&clientId=uff2457c5-bf3f-4&from=paste&id=u531f22ed&originHeight=553&originWidth=1104&originalType=url&ratio=1&rotation=0&showTitle=false&size=155674&status=done&style=stroke&taskId=u434b7c5c-6728-4563-a832-9779247929c&title=)
### Linux环境搭建（centos7）
centos下安装相对windows来说，会复杂一些，会遇到一些问题，不过大家不用担心，本文档后面会提供基于docker的环境构建和部署，会简单很多。这里只是让大家从0开始体验一centos下搭建paddleocr环境。
#### python安装
**1.安装依赖包**
```shell
yum install zlib-devel bzip2-devel openssl-devel ncurses-devel sqlite-devel readline-devel tk-devel gcc make mysql-devel gcc-devel python-devel -y
```
**2.再执行安装libffi-devel，不安装会导致pip安装失败**
```shell
yum install libffi-devel wget -y
```
缺少这一步，会出现 ModuleNotFound：No module named ‘_ctypes’
** 3.下载python3.8包，并解压**
```shell
wget https://www.python.org/ftp/python/3.8.1/Python-3.8.1.tgz
```
**4.安装python3.8**
```shell
tar -zxvf Python-3.8.1.tgz
cd Python-3.8.1/
./configure ; make && make install
```
**5.配置环境变量**
输入"python3.8 -V", 打印3.8.1 说明3.8.1安装成功
输入"python3 -V",看python3对应的版本；
例如阿里云服务器会默认按照 python2.7 (对应python -V) 和 python3.6.x (对应python3 -V)
```shell
mv /usr/bin/python /usr/bin/python.bak
ln -s /usr/local/bin/python3.8 /usr/bin/python
mv /usr/bin/pip /usr/bin/pip.bak
ln -s /usr/local/bin/pip3.8 /usr/bin/pip
```
**6.验证**
输入 "python -V" , 输出 3.8.1 环境变量配置成功
输入 "pip -V", 输出19.2.3 ，配置成功
**7.配置yum**
上面操作完成后，输入yum会报错如下，是因为yum依赖python2.7，现在版本改成3.8.1了，因此会出错
```shell
yum update File “/usr/bin/yum”, line 30 except KeyboardInterrupt, e: ^ SyntaxError: invalid syntax
```
**编辑文件/usr/libexec/urlgrabber-ext-down**
```shell
vim /usr/libexec/urlgrabber-ext-down
```
改成和下图一样：
![3117216567-64f6dfe1adc1f.webp](https://cdn.nlark.com/yuque/0/2023/webp/13011155/1695370289933-34ecbd3a-2fc3-4ee6-a79f-4826f37237ed.webp#averageHue=%23090b08&clientId=uf5b1a772-3b41-4&from=drop&id=udfa7dd96&originHeight=589&originWidth=903&originalType=binary&ratio=1&rotation=0&showTitle=false&size=58074&status=done&style=stroke&taskId=u00c55b13-a02c-4c89-ad7f-1a53f29faee&title=)
编辑 /usr/bin/yum ,和上一步同一样的修改
**8.安装依赖**
```shell
sudo yum install libxml2-devel libxslt-devel -y
sudo pip install lxml
sudo pip install requests
```
#### 安装PaddlePaddle
参考：[https://www.paddlepaddle.org.cn/documentation/docs/zh/install/pip/linux-pip.html](https://www.paddlepaddle.org.cn/documentation/docs/zh/install/pip/linux-pip.html)
**1.确认pip版本，要求 pip 版本为 20.2.2 或更高版本，如果版本低那么更新**
```shell
pip -V
```
如果pip版本低于20.2.2
```shell
pip install --upgrade pip
```
**2.需要确认 Python 和 pip 是 64bit，并且处理器架构是 x86_64（或称作 x64、Intel 64、AMD64）架构。**
下面的第一行输出的是”64bit”，第二行输出的是”x86_64”、”x64”或”AMD64”即可：
```shell
python -c "import platform;print(platform.architecture()[0]);print(platform.machine())"
```
**3.安装paddlepaddle**
```shell
python -m pip install paddlepaddle==2.5.1 -i https://mirror.baidu.com/pypi/simple
```
**4.验证paddlepaddle安装**
安装完成后您可以使用命令 python 进入 python 解释器，输入import paddle ，再输入 paddle.utils.run_check()
如果出现PaddlePaddle is installed successfully!，说明您已成功安装。
**问题处理**
验证是否成功安装时可能会出现如下错误：
![316742536-64f6e72c35101_fix732.webp](https://cdn.nlark.com/yuque/0/2023/webp/13011155/1695373618778-fa83cd73-cb48-4281-8581-46c82b7b11db.webp#averageHue=%234b473f&clientId=u3947c374-02fb-4&from=drop&id=ua6b08602&originHeight=496&originWidth=1098&originalType=binary&ratio=1&rotation=0&showTitle=false&size=74980&status=done&style=stroke&taskId=u231bad33-92b5-4237-b5cd-91c84611b4a&title=)
说明缺少 GLIBCXX_3.4.20依赖。
解决方法参考：[https://www.jianshu.com/p/050b2b777b9d](https://www.jianshu.com/p/050b2b777b9d) ，具体执行步骤如下：
```shell
1. 查看系统版本
strings /usr/lib64/libstdc++.so.6 | grep  ，输出如下：
GLIBCXX
GLIBCXX_3.4
GLIBCXX_3.4.1
GLIBCXX_3.4.2
GLIBCXX_3.4.3
GLIBCXX_3.4.4
GLIBCXX_3.4.5
GLIBCXX_3.4.6
GLIBCXX_3.4.7
GLIBCXX_3.4.8
GLIBCXX_3.4.9
GLIBCXX_3.4.10
GLIBCXX_3.4.11
GLIBCXX_3.4.12
GLIBCXX_3.4.13
GLIBCXX_3.4.14
GLIBCXX_3.4.15
GLIBCXX_3.4.16
GLIBCXX_3.4.17
GLIBCXX_3.4.18
GLIBCXX_3.4.19
GLIBCXX_DEBUG_MESSAGE_LENGTH

发现少了GLIBCXX_3.4.20，解决方法是升级libstdc++.

2. sudo yum provides libstdc++.so.6 ，输出如下：
Loaded plugins: fastestmirror, langpacks
Determining fastest mirrors
libstdc++-4.8.5-39.el7.i686 : GNU Standard C++ Library
Repo        : base
Matched from:
Provides    : libstdc++.so.6

3. cd /usr/local/lib64
# 下载最新版本的libstdc.so_.6.0.26
sudo wget http://www.vuln.cn/wp-content/uploads/2019/08/libstdc.so_.6.0.26.zip
unzip libstdc.so_.6.0.26.zip
# 将下载的最新版本拷贝到 /usr/lib64
cp libstdc++.so.6.0.26 /usr/lib64
cd  /usr/lib64
# 查看 /usr/lib64下libstdc++.so.6链接的版本
ls -l | grep libstdc++ ，输出如下：
libstdc++.so.6 ->libstdc++.so.6.0.19
# 删除/usr/lib64原来的软连接libstdc++.so.6，删除之前先备份一份
sudo rm libstdc++.so.6
# 链接新的版本
sudo ln -s libstdc++.so.6.0.26 libstdc++.so.6
# 查看新版本，成功
strings /usr/lib64/libstdc++.so.6 | grep GLIBCXX ，输出如下：
...
GLIBCXX_3.4.18
GLIBCXX_3.4.19
GLIBCXX_3.4.20
GLIBCXX_3.4.21
GLIBCXX_3.4.22
GLIBCXX_3.4.23
GLIBCXX_3.4.24
GLIBCXX_3.4.25
GLIBCXX_3.4.26
GLIBCXX_DEBUG_MESSAGE_LENGTH
...
```
#### 安装PaddleOCR
同windows下步骤一样，这里不再赘述
**错误处理，执行paddleocr命令时可能会有以下错误：**
错误1：缺少libGL ，“ImportError: libGL.so.1: cannot open shared object file: No such file or directory”，执行如下指令解决：
```shell
yum install mesa-libGL.x86_64
```
错误2：urllib3版本高，依赖的openssl版本低，
ImportError: urllib3 v2.0 only supports OpenSSL 1.1.1+, currently the 'ssl' module is compiled with 'OpenSSL 1.0.2k-fips  26 Jan 2017'. See: https://github.com/urllib3/urllib3/issues/2168
解决：降低urllibs3的版本，执行如下指令：
```shell
pip install urllib3==1.26.16 -i https://pypi.tuna.tsinghua.edu.cn/simple
```

## PaddleOCR命令讲解
前面在验证PaddleOCR安装是否成功时已经简单使用过命令了，下面我们详细的讲解一下paddleocr指令。查看具体使用方法：
```json
paddleocr --help
```
1.检测+方向分类器+识别全流程（一般都是用这个）：
```shell
paddleocr --image_dir ./imgs/11.jpg --use_angle_cls true --use_gpu false --ocr_version PP-OCRv3
```
**--image_dir**  识别图片的路径
**--use_angle_cls true** 设置使用方向分类器识别180度旋转文字
**--use_gpu false** 设置不使用GPU
**--ocr_version PP-OCRv3** 制定模型 ， PaddleOCR默认会下载使用最新的模型，当前是PP-OCRv3, 这里只是告诉大家这个参数怎么用
输出结果是结果是一个list，每个item包含了文本框（坐标），文字和识别置信度：
```shell
[[[28.0, 37.0], [302.0, 39.0], [302.0, 72.0], [27.0, 70.0]], ('纯臻营养护发素', 0.9658738374710083)]
......
```
文本框分别表示 左上、右上、右下、左下 顺时针方向矩形框的四个角像素坐标

2.单独使用检测：设置 **--rec为false**
```shell
paddleocr --image_dir ./imgs/11.jpg --rec false
```
结果是一个list，每个item只包含文本框：
```shell
[[27.0, 459.0], [136.0, 459.0], [136.0, 479.0], [27.0, 479.0]]
[[28.0, 429.0], [372.0, 429.0], [372.0, 445.0], [28.0, 445.0]]
......
```
3.单独使用识别：设置**--det为false**
```shell
paddleocr --image_dir ./imgs_words/ch/word_1.jpg --det false
```
结果是一个list，每个item只包含识别结果和识别置信度
```shell
['韩国小馆', 0.994467]
```
4.指定语言 **--lang**（默认也能识别英文，制定语言效果会更好）
```shell
paddleocr --image_dir ./imgs_en/254.jpg --lang=en
```
5.paddleocr也支持输入pdf文件，并且可以通过指定参数**page_num**来控制推理前面几页，默认为0，表示推理所有页
```shell
paddleocr --image_dir ./xxx.pdf --use_angle_cls true --use_gpu false --page_num 2
```

## Python脚本中使用PaddleOCR

- 检测+方向分类器+识别全流程，只需要下面三行代码
```python
#导入依赖
from paddleocr import PaddleOCR
#创建PaddleOCR对象，只需要在初始化时执行一次该语句
ocr = PaddleOCR(use_angle_cls=True, det=False, use_gpu=False)
#识别图片返回结果，cls=True 表示识别旋转180度的文字，如果没有文字旋转180度，那么
#可以cls=False，这样会提升性能，旋转90度和270度也能够识别
result = ocr.ocr(imgPath, cls=True)
```
## 识别服务开发
![ocr实战架构图.jpg](https://cdn.nlark.com/yuque/0/2023/jpeg/13011155/1696646802468-b02332df-5f76-4549-8e37-cbc4f86b5ae2.jpeg#averageHue=%23f7e6ce&clientId=u636b264f-584e-4&from=drop&id=NC5Qi&originHeight=765&originWidth=1624&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=119499&status=done&style=stroke&taskId=udd041dbf-133c-4760-8f42-b0ed2aef5f8&title=)
### PaddleOCR内部接口开发
#### 开发工具
开发工具为PyCharm 社区版
#### 代码开发
新建一个项目，将下面的代码，拷贝到main.py(PyCharm默认会新建一个)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696667358141-8403876f-2819-4f10-9b49-4b3c68f0b695.png#averageHue=%233c4043&clientId=u88108e0d-ebc3-4&from=paste&height=465&id=TRXTW&originHeight=937&originWidth=1249&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=70220&status=done&style=stroke&taskId=u898d76fd-1844-4930-a712-b2e95d308a0&title=&width=619.4000244140625)
下面这段paddle_ocr_web.py python代码主要是实现：通过flask创建web容器，并通过 /ocr 接口调用 paddleocr进行图片识别，并通过json格式返回识别结果。
```python
import json
import logging
from paddleocr import PaddleOCR
from flask import Flask, request, jsonify

# 设置日志输出等级
logging.basicConfig(level=logging.DEBUG)

# 只需要初始化加载一次
ocr = PaddleOCR(use_angle_cls=True)

# 所有的Flask都必须创建程序实例，程序实例是Flask的对象，一般情况下用如下方法实例化
app = Flask(__name__)

# https://cloud.tencent.com/developer/article/1539199 flask request常用方法
# @app.route('/ocr', methods=["GET"])
# def paddleOcr():
#     params = request.args
#     # 获取参数
#     imgPath = params.get("imgPath", 0)
#     logging.info("paddle ocr img %s", imgPath)
#     result = ocr.ocr(imgPath, cls=True)
#     return jsonify(data=result), 200

@app.route('/ocr', methods=["POST"])
def paddleOcr():
    # 获取传入参数
    data = json.loads(request.data)
    # 获取传入的图片路径
    imgPath = data["imgPath"]
    logging.info("paddle ocr2 imgPath %s", imgPath)
    #进行ocr识别
    # 如果不需要检测坐标，那么可以设置 det=False,result = ocr.ocr(imgPath, cls=True, det=False)
    result = ocr.ocr(imgPath, cls=True)
    #识别结果通过json格式返回
    return jsonify(data=result), 200


#  程序实例用run方法启动flask集成的web服务器
if __name__ == '__main__':
    # 可以返回中文字符,否则汉字会以Unicode编码形式返回
    app.config['JSON_AS_ASCII'] = False
    # 接收所有IP的请求,debug=True 代码修改，web容器会重启
    app.run(host='0.0.0.0', debug=True, port=8888)
```

- import 表示导入整个module(模块)，一个.py文件就是一个module
- from A import B 表示导入A模块中的B（可以为方法、类）
- flask是python实现的web框架，类似Tomcat

             ![](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696664523936-11347d46-d464-4f1d-8c04-7b49f64fc454.png#averageHue=%23f6eacf&clientId=u88108e0d-ebc3-4&from=paste&height=324&id=u59905347&originHeight=357&originWidth=693&originalType=url&ratio=1.25&rotation=0&showTitle=false&status=done&style=stroke&taskId=uf2d39be9-7b2a-41d0-a6b7-da5877bd086&title=&width=629)
       1.如果是json格式的请求数据，则是采用request.data来获取请求体的字符串。
       2.如果是form表单的请求体，那么则可以使用request.form来获取参数。
       3.如果是url参数，例如：url?param1=xx&param2=xx，那么则可以使用request.args来获取参数。
       4.如果需要区分GET\POST请求方法，则可以使用request.method来进行判断区分
       5.参考：[https://cloud.tencent.com/developer/article/1539199](https://cloud.tencent.com/developer/article/1539199)，解释的很清楚
       6.@app.route 声明一个接口，指定请求方法和U请求路径

- 作为 Python 的内置变量，**__name__**它是每个 Python 模块必备的属性，但它的值取决于你是如何执行这段代码。通过**__name__** 变量，可以判断出这时代码是被直接运行，还是被导入到其他程序中去了。当直接执行一段脚本的时候，这段脚本的 **__name__**变量等于 **'__main__'**，当这段脚本被导入其他程序的时候，**__name__** 变量等于脚本本身的名字。
#### 测试
运行main.py,在项目下创建test文件夹，并拷贝一张图片到该文件下
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696668546072-d1b49c4c-b808-4da0-8d84-1d1d1052368d.png#averageHue=%233c3f42&clientId=u88108e0d-ebc3-4&from=paste&height=488&id=ub27fe506&originHeight=610&originWidth=1282&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=44335&status=done&style=stroke&taskId=u3226f7f4-6313-49fc-b8e6-02d0b65abbc&title=&width=1025.6)
使用postman请求，如下图：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/13011155/1696668411282-2c325aab-dcff-47ab-b182-a1b6326b313d.png#averageHue=%23fbfbfb&clientId=u88108e0d-ebc3-4&from=paste&height=588&id=u46984c99&originHeight=735&originWidth=956&originalType=binary&ratio=1.25&rotation=0&showTitle=false&size=51557&status=done&style=stroke&taskId=u9f887d4c-3d7a-443d-9efb-c0fb0838779&title=&width=764.8)
返回结果：
```json
{
  "data": [
    [
      [
        [
          [
            20.0,
            35.0
          ],
          [
            190.0,
            35.0
          ],
          [
            190.0,
            61.0
          ],
          [
            20.0,
            61.0
          ]
        ],
        [
          "Docker部署",
          0.9223976731300354
        ]
      ]
    ]
  ]
}
```
### 
**此文档是**《[OCR文字识别实战教程-零基础](https://www.bilibili.com/video/BV1RK411b7DU/)，SpringBoot结合PaddleOCR》 地址：[https://www.bilibili.com/video/BV1RK411b7DU/?vd_source=b4307343204f5c0271966f7fe276f0eb](https://www.bilibili.com/video/BV1RK411b7DU/?vd_source=b4307343204f5c0271966f7fe276f0eb)，**教程对应的部分文档，全部文档共89页，剩余文档和完整源代码 请关注B站视频**
