
# WebApp Api for Android

WebApp Api is Android extended Webview Client are flexible, safe and easier request your Api and supports for using **Cross-Origin Resource Sharing (CORS)**.

This sample WebApp app uses App Message integration for send and receive messages through broadcasts.

Through the integrated AppMessage allow request Api by activity A and Return the Response to activity B or A.

_**minimum SDK version**: Android 5.0 (API 21) (recommended), but you also can run on Android 4.4 (API 19) and higher._

<h1>Download Android Studio and Configure</h1>


Now Webapp Api can run with your favorite android version with full compatible with latest versions:<br>
- Android Studio Iguana(Release candidate) [.zip](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.2.1.21/android-studio-2023.2.1.21-windows.zip) [.exe](https://redirector.gvt1.com/edgedl/android/studio/install/2023.2.1.21/android-studio-2023.2.1.21-windows.exe)<br>
- Android Studio Jellyfish(Preview - canary) [.zip](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.3.1.8/android-studio-2023.3.1.8-windows.zip) [.exe](https://redirector.gvt1.com/edgedl/android/studio/install/2023.3.1.8/android-studio-2023.3.1.8-windows.exe)<br>
- Android Studio Hedgehog(Stable - current release) [.zip](https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2023.1.1.28/android-studio-2023.1.1.28-windows.zip) [.exe](https://redirector.gvt1.com/edgedl/android/studio/install/2023.1.1.28/android-studio-2023.1.1.28-windows.exe)<br>

Android Studio download archives [here](https://developer.android.com/studio/archive)<br>
Install Android Studio [here](https://developer.android.com/studio/install)

**Choose your download version**
* [Android Studio Preview download page](https://developer.android.com/studio/preview)
* [Android Studio Stable download page](https://developer.android.com/studio)

<h3>SDK Location</h3>
Please follow the below steps:

    Go to Android Studio and open WebApp Api Project then go to the android directory and open the following file:

    local.properties

    click on the line ico as in the image below to open the settings dialog to change the directory 
    
![sdk2](https://github.com/thiagoschnell/webappapi/assets/78884351/58b3bff0-5f51-4ae3-81be-6792863ec737)

<h3>Gradle sync - Android Gradle Plugin (AGP)</h3>
   If a message appears in Android Studio like the image below
   
<img alt="alt_text" width="300px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/94f56e26-32be-41bb-b24d-0c8b2948f9aa" /> <br>

Then click "Sync project" to finish the configuration.


## License

This repository is available under the [MIT License](https://github.com/thiagoschnell/webappapi/blob/main/LICENSE).

## Developer sources


Android Integrations such as **App Message**, **Webview** has also provides demo with detailed usage.
* [App Message demo](https://github.com/after-project/appmessage/)
* [Webview the basics demo](https://github.com/after-project/webview/)

Server Files for this WebApp app sample
* [PHP files](https://github.com/after-project/webappapi-php/)

Shop Site Demo for usage example purpouses
* [Real App Example site files](https://github.com/after-project/site-realappexample/)
* [Real App Example android source code](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/RealAppExample)

## Documentation for CORS
* [Origin](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Origin)
* [Same-origin policy](https://developer.mozilla.org/en-US/docs/Web/Security/Same-origin_policy)
* [Access-Control-Allow-Origin](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Access-Control-Allow-Origin)


## User guide
* [user guide page](https://github.com/thiagoschnell/webappapi/wiki/User-Guide) This guide contains examples on how to use WebApp Api in your code

## Examples

**Demo usage of one Real App Example**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/ade0c69e-a7b7-404e-b552-04bae45b29fe" />

<h3>**Zip Website Example**</h3>

Create a app and deliver the mobile version of your website, <br>
implement webappapi to make requests, <br>
load from zip your website, create a new version for the website from update.zip by url to download and install it, instant patch the website files from a external link patch.zip  <br>
Website to your Android app made easy its just need a zip file! [Zip Website files](https://github.com/after-project/zip-website), [Demo example here](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/ZipWebsiteToWebViewExample)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/7c29d543-c431-47c9-856b-6464f6a096c2" />

<h3>**Zip Website using CORS Example**</h3>

This is example of Zip Website  using CORS Example shows how setup allowed CORS domains for incoming request links in the websitewithcors.zip files. [Example here](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/ZipWebsiteToWebViewExampleWithCORS.java), [Apache MultiViews documentation](https://httpd.apache.org/docs/2.2/content-negotiation.html#multiviews)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/296e9f05-02a8-4067-9e9e-eea506a894c7" />

**Internet Connection - Realtime Network Status**<img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" /> <img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" />

Check the internet status in realtime before requesting any url

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/8271d9fc-846f-4509-827e-78810f3e4d9e" />

**UPDATE - Job Scheduler CheckPurchaseStatus**

By default BackoffPolicy.LINEAR run as 10,20,30 seconds and multiply it by x times will happen, ex: 10s *  how many times the work has been retried (for BackoffPolicy.LINEAR), and for BackoffPolicy.Exponential will be even longe.<br>
Now this example has updated to run dynamically 10,20,30 seconds.

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/479c7f78-65ab-4096-aa72-92261e73c4d1" />

**Download Checksum**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0ba40731-422d-45b0-81e7-88971b4ac5db" />

**Stream Download Example**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/8ff510b5-7671-4b4c-aa2f-eedfcde03146" />

**Download PDF Example**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0d59ed93-aef4-4a21-9cbe-c95f3526c7d1" />

**Download Image Example**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/54ee19af-486f-443f-a954-242c688d5054" />

**New Response Live Data**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/e2c49232-6588-4774-a0b1-7091131ecc22" />

**JobScheduler Example**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/9eeddbd6-bb88-4b25-861c-9deadf8e799b" />

**LoadDataWithBaseUrl Request**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/2b7dc25f-8b1a-4854-b2c7-70c91147281c" />

**Single Request**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/46e95abb-4bd2-4614-97b6-cc0c59d52330" />


**On Demand Requests**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/e2bfec3a-66dd-4c24-9dc7-b3372c27e735" />


**On Demand "Parallel" Requests**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/fdea0168-75a7-4b7f-96f6-f505abfb0589" />


**REQUEST WITH NO INTERNET**

![webapp-api-example1-connection_error](https://github.com/thiagoschnell/webappapi/assets/78884351/831a7668-d758-4bdd-bb09-7747205a7308)


**LOADING WITH NO INTERNET**

![webappapi-example1-load-error](https://github.com/thiagoschnell/webappapi/assets/78884351/05411355-a063-44a8-879f-168c9c6ef562)

