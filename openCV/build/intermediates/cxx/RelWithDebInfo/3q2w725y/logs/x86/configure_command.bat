@echo off
"C:\\Users\\rsroh\\AppData\\Local\\Android\\Sdk\\cmake\\3.18.1\\bin\\cmake.exe" ^
  "-HS:\\Android Projects\\MyCamera\\openCV\\libcxx_helper" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=24" ^
  "-DANDROID_PLATFORM=android-24" ^
  "-DANDROID_ABI=x86" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86" ^
  "-DANDROID_NDK=C:\\Users\\rsroh\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_ANDROID_NDK=C:\\Users\\rsroh\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620" ^
  "-DCMAKE_TOOLCHAIN_FILE=C:\\Users\\rsroh\\AppData\\Local\\Android\\Sdk\\ndk\\23.1.7779620\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=C:\\Users\\rsroh\\AppData\\Local\\Android\\Sdk\\cmake\\3.18.1\\bin\\ninja.exe" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=S:\\Android Projects\\MyCamera\\openCV\\build\\intermediates\\cxx\\RelWithDebInfo\\3q2w725y\\obj\\x86" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=S:\\Android Projects\\MyCamera\\openCV\\build\\intermediates\\cxx\\RelWithDebInfo\\3q2w725y\\obj\\x86" ^
  "-DCMAKE_BUILD_TYPE=RelWithDebInfo" ^
  "-BS:\\Android Projects\\MyCamera\\openCV\\.cxx\\RelWithDebInfo\\3q2w725y\\x86" ^
  -GNinja ^
  "-DANDROID_STL=c++_shared"
