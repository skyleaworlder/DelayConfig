# DelayConfig

适用于 `aosp-12`，基于对 framework/base/core/java/android 中代码的修改完成延迟操作。

## 一、使用方法

### 1. 运行对应版本的 avd

目前暂时以 AOSP 的 system.img 等镜像作为运行 avd 的镜像。

```shell
source build/envsetup.sh
lunch sdk_phone_x86_64
emulator -no-window -verbose -writable-system
```

### 2. 安装需要的 android app

```shell
adb install -r APP_APK
adb install -r APP_ANDROIDTEST_APK
# e.g adb install -r AntennaPod-debug.apk
# e.g adb install -r AntennaPod-androidTest.apk
```

### 3. 运行对应的 instrument

第一次运行只是记录该 test class 的运行会执行到的点位，并不延迟执行。

```shell
adb shell am instrument -w -e class "INSTRUMENT_NAME.SHORT_CLASS_NAME[#METHOD_NAME]" ANDROIDTEST_RUNNER_FULLY_NAME
# e.g. adb shell am instrument -w -e class de.test.antennapod.ui.PreferencesTest de.test.antennapod/androidx.test.runner.AndroidJUnitRunner
```

重复执行该命令，DelayConfig 会自动产生延迟，使得该 app 执行到相同位置时延迟执行。

### 4. 执行相同 app 的其他测试类 / 方法

在进程绑定 app 时，应该没有办法获取测试类 / 方法的信息。所以如果想要运行同一 app 的其他测试类 / 方法，需要：

```shell
adb pull /data/user/0/${app name}/files/ .
adb shell rm /data/user/0/${app name}/files/*.xml
```

将先前其他测试类 / 方法的运行结果 pull 到本地，删除 avd 中的配置输出文件，此后再指定新的测试类 / 方法：

```shell
adb shell am instrument -w -e class "INSTRUMENT_NAME.NEW_SHORT_CLASS_NAME[#NEW_METHOD_NAME]" ANDROIDTEST_RUNNER_FULLY_NAME
```

## 二、原因

DelayConfig 的延迟工作是基于 /data/user/0/${app name}/files 中的配置文件完成的。

在 framework/base/core/java/android/app/ActivityThread.java 中的 handleBindApplication 与 finishInstrumentation 中分别插入了 `readConfig` 与 `writeConfig` 来读取配置文件。

在 AOSP 源代码 (framework/base/core/java/android) 中插入了 `DelayConfigHelper.sleep()`，该方法：

* 在未读取到配置文件时，会负责记录点位；
* 在读取到配置文件时，会执行延迟操作。
