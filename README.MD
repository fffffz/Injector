##### 集成
**build.gradle 配置**

````
buildscript {
    dependencies {
        ...
        classpath fileTree(dir: 'plugins', include: ['*.jar'])
    }
}
````

**app/build.gradle 配置**

````
apply plugin: 'com.qiyi.video.injector'

injector {
    leakCanaryClass = "org.icegeneral.injector.sample.LeakCanaryAdapter"
    watchFragment = true

    trackClass = "org.icegeneral.injector.sample.Tracker"
    trackActivity = true

    trackTargetFile = "C:/Code/Injector/sample/track.json"
}
````

---

**Activity**

onResume  中插入 Tracker.track("onResume "+ getClass().getName())

onPause   中插入 Tracker.track("onPause  "+ getClass().getName())

**Fragment**

onDestroy 中插入 LeakCanaryAdapter.watch(this)

**trackTarget**

读取json配置文件，其中 argIndexes 1 代表 onClick 方法的第 1 个参数，0 代表 this
className 不配置表示所有在 class 中的 methodName 方法进行插桩
sample 中的配置表示

````
public void onClick(View view) {
    Tracker.track(Utils.getClickText(view));
    ...
}
````