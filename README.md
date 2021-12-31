## Shadowsocks
shadowsocks is a fast tunnel proxy that helps you bypass firewalls.   
shadowsocks-java is a implementation of shadowsocks protocol that base on java's netty framework.

Features:
- TCP & UDP support
- Stream & AEAD cipher support

Install
------

**Build from source code**

1. Install JDK8 or higher
2. Install Maven
3. Clone repository
4. Go to the project directory and run `mvn package`
5. The executable file is in `target/bin` directory 

**Download**

Coming soon

Usage
-----

**command**

```shell
usage: sslocal [FLAGS] [OPTIONS]

flags:
 -h,--help      Prints help info
 -v,--version   Show version number

options:
 -b,--local-addr <arg>    Local address, listen only to this address if specified. (format: host:port)
 -c,--config <arg>        Configuration file
 -k,--password <arg>      Password
 -m,--method <arg>        Cipher method. ['rc4-md5',
                          'aes-128/192/256-cfb/ctr','aes-128/256-gcm','chacha20-ietf-poly1305']
 -s,--server-addr <arg>   Server address. (format: host:port)
 -t,--timeout <arg>       Connection timeout

```

**ss-local**   

Start ss-local through command line argument.

```shell
./sslocal.sh -k 123456 -m aes-256-gcm -b 127.0.0.1:1080 -s 127.0.0.1:8383
```

You can also start ss-local by loading the configuration file.

```shell
./sslocal.sh -c config.json
```



**ss-server**

Start ss-server through command line argument.

```shell
./ssserver.sh -k 123456 -m aes-256-gcm -s 0.0.0.0:8383
```

You can also start ss-server by loading the configuration file.

```shell
./ssserver.sh -c config.json
```



## Contact

you can contact me by the following ways

Email: bigbyto@gmail.com

Blog: https://wiyi.org



## License
Copyright (c) 2021 shadowsocks-java

shadowsocks-java is free and open source software, licensed under Apache License v2.0 . See LICENSE for full details.

