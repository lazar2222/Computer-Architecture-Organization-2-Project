cd aquila-build
rm .\examples\perfTest\perfTest.exe
cmake -G "MinGW Makefiles" ..\aquila-src\
make
make install
make examples
.\examples\perfTest\perfTest.exe
.\examples\perfTest\perfTest.exe
.\examples\perfTest\perfTest.exe
cd ..