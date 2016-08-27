# Pomu

## Loading resources on demand

This api targets problems around APK sizes, and the problem of having a lot of local resources which occupy space, but probably are used only at really few places.

What it does is, when your application is in need of a resource, it will at that moment download it and take care of it for you, without having to deal with more problems than solutions.

### Usage:

Initialize Pomu in your Application's onCreate().

Also, if your application targets API levels lower than 19, where Dalvik is the VM, its highly recommended to use Fresco for image loading.

```Java
   //In your applications onCreate()
   Fresco.initialize(this);
   Pomu.initialize(this);

   //Optional
   //If you plan on using custom configurations (like custom cache / network client)
   Pipeline.getInstance().setConfigurations(configurations); //Or wherever you want, but to have a cohesive configuration across all resources its better here :)
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

### Features:

- [x] Persistable resources once downloaded, to avoid high network traffic and bad ux for the user.
- [x] Fresco and native ImageView support (for memory issues in Dalvik systems Fresco was selected over Picasso)
- [x] Screen density images format support
- [x] Custom cache. By default it's a LRU cache up to 15 mb (maxDiskSize can also be tweaked if you still want to have LRU)
