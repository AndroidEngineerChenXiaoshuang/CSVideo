# CSVideo1.0

## 此库主要是面向录制视频，拍摄相片所仿照微信开发的,

主要包含了以下功能:
 1. 长按录像,短按拍照.
 2. 拍照剪切
 3. 高仿微信长按录制视频，高仿微信进度条效果

**集成项目方法:**

可以clone本库到你的电脑中，然后依赖本库到你的项目中，至于依赖方法可以产考[Android Sutido如何添加项目为依赖,详细图文](http://www.jianshu.com/p/18f8e2e124d1)这篇文章中的方法。但是这样配置项目依赖比较繁琐，不是现在主流的使用Gradle来添加项目的方式，还请大家海涵，作者将会在后续版本中添加此功能，并继续完善和维护!

**使用方法:**

将CSVideo依赖到你的项目后，就可以进行开发了，在你的项目逻辑中如果需要使用到拍摄或者录制的地方添加最简单的代码:
```
  Intent intent = new Intent();
  intent.setClass(this, CsVideo.class);
  startActivityForResult(intent, START_CSVIDEO);
```
并在代码中添加
```
@Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (requestCode) {
          case START_CSVIDEO:
              if (resultCode == RESULT_OK) {
                  String videoPaths = data.getStringExtra("videoPath");
                  String imagePath = data.getStringExtra("imagePath");
                  /**
                   * 在下面进行你的逻辑判断
                   * csVideo只会返回一个路径,不是视频路径,就是视频路径
                   * 当然后面还会继续优化,比如视频第一帧图,或者图片原图路径和裁剪路径一起返回
                   */

                  if (!TextUtils.isEmpty(videoPaths)) {

                  } else if (!TextUtils.isEmpty(imagePath)) {

                  }
              }
              break;
      }
  }
```

使用最原生的方式来调用CSVideo，并在onActivityResult回调中进行逻辑处理，还是非常简单的🤓


  
