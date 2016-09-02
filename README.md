# Pomu

## Loading resources on demand

This api targets problems around APK sizes, specifically having a lot of local resources in every density, which occupy space, but probably are used only at once place.

This will let you download resources on demand, without having to deal with more problems than solutions.

### Minimum Api Level: 11

### Download

In your module gradle file add

```Java
	compile 'com.saantiaguilera:Pomu:1.0.3'
```

If its not found (because I have to wait for bintray to add it to jcenter) apply in the module gradle file:

```Java
repositories {
	mavenCentral()
	maven {
		//Since bintray hasnt already publish it
		url  "http://dl.bintray.com/saantiaguilera/maven"
	}
}
```

### Usage:

Initialize Pomu in your Application's onCreate().

Also, if your application targets API levels lower than 19, where Dalvik is the VM, its highly recommended to use Fresco for image loading.

```Java
   //In your applications onCreate()
   Pomu.initialize(this);

   //If you use fresco.
   Fresco.initialize(this);

   //Optional
   //If you plan on using custom configurations (like custom cache / network client)
   Pipeline.getInstance().setConfigurations(configurations); //Or wherever you want, but to have a cohesive configuration across all resources its better here :)
   //Or on initialization by doing
   Pomu.initialize(this, configurations);
   //Careful Pomu should only be initialized once.
```

For loading a resource just:

```Java
    Pomu.create(context)
        .url(urlToLoad)
        .callback(ifYouWantCallbacks)
        .into(yourImageView);
```

If you are using fresco, this is fully transparent and we will take care of it :)

For customizing fresco loadings, you can provide a 

```Java
   Pomu.create(context)
        ....
        .controller(FrescoImageController.create(this)
           .autoRotate(true)
           .resize(400, 400)
           .noCache()
           .progressiveRendering(true) 
           // and stuff...
        )...
```

If your server / host provides a format of images according to the density of screens

Kinda images are in the format of http://mylovelyhost.com/image-of-id-:id-in-density-:density_pixels

Eg. http://mylovelyhost.com/image-of-id-322345-in-density-400x400

You can give to Pomu a UrlDensityFormatter and have it do this for you instead of boilerplating:

```Java
    UrlDensityFormatter formatter = new UrlDensityFormatter() {
        @Override
        public String from(@NonNull ScreenDensity density) {
            switch (density) {
                case MDPI:
                    return "400x400";
                case XXXHDPI:
                    return "xhdpi";
                case TV:
                    return "idk-what-the-server-expects-in-the-url-for-a-tv";
                default:
                    return "200x200";
            }
        }
    };

    Pomu.create(context)
       ....
       .url("http://mylovelyhost.com/image-of-id-234234-in-density-%s", formatter)
       ....
```

If you want to just download a image (or images) without displaying it. For example because your app at start should have always some resources, or because you want them prior to showing them to avoid user seeing the loading. You can do:

```Java
	Pomu.create(context)
		.url(oneUrlToDownload)
		.url(another)
		.url(oneMore)
		.url(allTheOnesYouWant)
		...
		.get(); //Instead of into(imageView)
```

### Features:

- [x] Persistable resources once downloaded, to avoid high network traffic and bad ux for the user.
- [x] Fresco and native ImageView support (for memory issues in Dalvik systems Fresco was selected over Picasso)
- [x] Screen density images format support
- [x] Secure storage of resources (in app dir / mangle names)
- [x] Custom cache. By default it's a LRU cache up to 15 mb (maxDiskSize can also be tweaked if you still want to have LRU)
- [x] Getting resources on demand
- [x] Getting resources at any particular moment and caching them
- [x] Cache control (eviction can be done by user)
- [x] Status callbacks
