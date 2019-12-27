

# Thash - ថាស

A circular progress view

<img src="https://raw.githubusercontent.com/seanghay/thash/master/screenshots/thash.png" width="400" />

--------

## Installation

via Gradle

```gradle
dependencies {
    implementation 'com.seanghay:thash:0.1.0'
}
```

---------
## How to Use

### 1. Inflate view

```xml
<com.seanghay.library.ThashView
    android:id="@+id/thashView"
    android:layout_width="200dp"
    android:layout_height="200dp"
    app:progress="60"
    app:maxProgress="100"
    app:progressRoundedCap="true"
    app:progressWidth="26dp"
    app:progressColor="#F17237"
    app:indicatorAngleOffset="0"
    app:indicatorFraction=".4"
    app:indicatorColor="#CC501A"
    app:indicatorGravity="start"
    app:hideIndicator="false"
    app:progressBackgroundColor="#eee"
    app:progressBackgroundWidth="26dp"
/>
```


### 2. Progress changed events

```kotlin
thashView.progressChanged { progress, fraction ->

}
```

### 3. Set progress dynamically

```kotlin

// With smooth animation
thashView.animateProgress(50f)

// ValueAnimator options
thashView.animateProgress(50f) {
    duration = 400
    interpolator = MyInterpolator()
}

// No Animation
thashView.progress = 50f
```

### 4. Animate Angle
```kotlin
thashView.animateAngle()

// With ValueAnimator options
thashView.animateAngle {
    duration = 2000
    interpolator = MyInterpolator()
}
```
