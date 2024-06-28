# Resource Handler Spring Boot Starter

Spring Boot application integrates rate limitation quickly, to prevent too frequent accesses.

## Quickstart

- Import dependencies

```xml
    <dependency>
        <groupId>com.yookue.springstarter</groupId>
        <artifactId>resource-handler-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> By default, this starter will auto take effect, you can turn it off by `spring.resource-handler.enabled = false`

- Configure Spring Boot `application.yml` with prefix `spring.resource-handler` (**Optional**)

```yml
spring:
    resource-handler:
        resource-mapping:
            add-default-versional: true
            add-default-original: false
            versional:
                - '/foo/**': 'classpath:/foo/'
            original:
                - '/bar/**': 'classpath:/bar/'
        virtual-root:
            resource: 'classpath:/vroot/'
```

- **Optional feature**: If you don't like putting many files under the `src/main/resources` folder directly, you'd better try the `virtual-root` feature. This will add the virtual mapping "/vroot/**" to the `original` mapping, thus you can put files under the folder `src/main/resources/vroot`, then you can access them by `://domain/file`. This is useful for `favicon.ico`, `robot.txt`, etc.

- Annotate your (non-static)  method with `@RateLimited` annotation, done!

> The `keyType` attribute of the annotation, is the limitation ways, supports
  - IP address
  - session
  - username

- This starter needs to save the limitation data to somewhere, currently is
  - redis

## Document

- Github: https://github.com/yookue/resource-handler-spring-boot-starter

## Requirement

- jdk 1.8+

## License

This project is under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

See the `NOTICE.txt` file for required notices and attributions.

## Donation

You like this package? Then [donate to Yookue](https://yookue.com/public/donate) to support the development.

## Website

- Yookue: https://yookue.com
