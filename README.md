# NostalgiaLauncher Desktop
Minecraft PE Alpha versions launcher for Windows and Linux

### Functions
- Download and quickly switch between versions
- Add your own version sources and custom executables for launching versions
- World manager that allows editing world info
- Fast texture installer
- Instances
- And more..

### Used materials
- NostalgiaLauncher Desktop uses [Ninecraft](https://github.com/MCPI-Revival/Ninecraft) to run Minecraft PE versions (the default executable for launching versions).

- [SpoutNBT](https://github.com/zhuowei/SpoutNBT) for world management.

### Building and running:
You need JDK 8 or high to build and run the launcher

**Build:**
```shell
$ git clone https://github.com/NLauncher/NostalgiaLauncherDesktop.git
$ cd NostalgiaLauncherDesktop
$ mvn package
```
**Run builded version:**
```shell
$ java -jar target/NostalgiaLauncherDesktop.jar
```

### Screenshot
![Launcher Screenshot](screenshot.png)