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
    leakCanaryClass = "org/icegeneral/injector/sample/LeakCanaryAdapter"
    trackClass = "org/icegeneral/injector/sample/Tracker"
}
````

---

**Activity**

onCreate  中插入 Tracker.track("onCreate  "+getClass().getName())

onDestroy 中插入 Tracker.track("onDestroy "+getClass().getName())

**Fragment**

onCreate  中插入 Tracker.track("onCreate  "+getClass().getName())

onDestroy 中插入 Tracker.track("onDestroy "+getClass().getName()) 和 LeakCanaryAdapter.watch(this)