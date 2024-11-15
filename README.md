
# WebApp Api for Android

WebApp Api is Android extended Webview Client are flexible, safe and easier request your Api and supports for using **Cross-Origin Resource Sharing (CORS)**.

_**minimum SDK version**: Android 5.0 (API 21) (recommended), but you also can run on Android 4.4 (API 19) and higher._

* New support for After Execute integration is starting ![project](https://github.com/thiagoschnell/afterexecute) <br>

## Download Android Studio and Configure

Now Webapp Api can run with your favorite android version with full compatible with latest versions:

* [Download Android Studio & Configure SDK and AGP](https://github.com/thiagoschnell/webappapi/wiki/Download-Android-Studio-&-Configure-SDK-and-AGP) This guide page for download Android Studio and Configure SDK and AGP

## Android 15 - June 	Beta 3

 Android 15 ready
 
 see more details about android 15 preview [here](https://developer.android.com/about/versions/15/overview).

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b83f8a41-363d-4350-b446-f9e2cab57ef2" />


## License

This repository is available under the [MIT License](https://github.com/thiagoschnell/webappapi/blob/main/LICENSE).

## Developer sources


Android Integrations such as **App Message**, **Webview** has also provides demo with detailed usage.
* [App Message demo](https://github.com/after-project/appmessage/)
* [Webview the basics demo](https://github.com/after-project/webview/)
* [App Messenger demo](https://github.com/thiagoschnell/appmessenger)(NEW)
* [Shuffle Crypt demo](https://github.com/thiagoschnell/shufflecrypt)(NEW)
* [AfterExecute demo](https://github.com/thiagoschnell/afterexecute)(BETA)

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


<h3>Internet Connection2</h3><img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" /> <img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" /><img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" />


The new Internet Connection 2<b> is very similar like Firebase Connection State</b>, but firebase sometimes bug. 
however Internet Connection 2 works with logic operations, and confirm if there is already in offline and then when handle disconnection a timer is started to run next 60 seconds to confirm isConnected. ![InternetConnection2.java](https://github.com/thiagoschnell/webappapi/blob/main/app/src/main/java/com/after_project/webappapi/InternetConnection2.java)

<img alt="alt_text" width="192px" src="https://github.com/user-attachments/assets/c0590d0d-573f-466d-8148-e4ec893656ab" />

<br><br>

<h3>**Ultime Webapp Example**</h3>

The Ultimate WebApp Example uses the new AppMessenger.java and the service Messenger.java based of last example Messenger Service. With Appmessenger you can send request to messenger(Server service) and response back to other active client connection (AppMessenger) that you have created. Go to MessengerConnectionManager.java and move ![here](https://github.com/thiagoschnell/webappapi/blob/b01aee3a212fd287fa53a6e4653f2e9afe52f8b8/app/src/main/java/com/after_project/webappapi/UltimateWebAppExample/MessengerConnectionManager.java#L123) then Create a new ConnectionPolicy for the Class that you want to allow connect, you can choose ![CONNECTION_NORMAL ](https://github.com/thiagoschnell/webappapi/blob/b01aee3a212fd287fa53a6e4653f2e9afe52f8b8/app/src/main/java/com/after_project/webappapi/UltimateWebAppExample/MessengerConnectionManager.java#L126) or ![CONNECTION_MULTCLIENT ](https://github.com/thiagoschnell/webappapi/blob/b01aee3a212fd287fa53a6e4653f2e9afe52f8b8/app/src/main/java/com/after_project/webappapi/UltimateWebAppExample/MessengerConnectionManager.java#L125C9-L125C31).
Also, now all ![async requests](https://github.com/thiagoschnell/webappapi/blob/b01aee3a212fd287fa53a6e4653f2e9afe52f8b8/app/src/main/java/com/after_project/webappapi/UltimateWebAppExample/Messenger.java#L143) information are crypted by ShuffleCrypt.java.
![Ultimate Webapp example files here](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/UltimateWebAppExample) , ![SuffleCrypt demo here](https://github.com/thiagoschnell/shufflecrypt), ![AppMessenger demo here](https://github.com/thiagoschnell/appmessenger)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/52bfff87-3e38-4e59-9989-7eaa25475c16" />

<h3>Demo usage of one Real App Example</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/ade0c69e-a7b7-404e-b552-04bae45b29fe" />

<h3>Messenger Service Example</h3>

Messenger Service are written from zero and is a (IPC) same as WebApp service but the diferente is the WebApp Service run on inside activity only and Messenger Service run in application for all activities.<br> In this example we extended the application with Messenger Service to become usage with Livedata, when this listening changes they send request as messenger client and then execute it inside messenger server that are running the webapp and back it to handler click callback. ![Example usage](https://github.com/thiagoschnell/webappapi/blob/main/app/src/main/java/com/after_project/webappapi/MessengerService/MessengerServiceActivity.java), ![Messenger Service files](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/MessengerService)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0db1d4da-b12c-4f38-bd41-4b296d08af07" />

<h3>WebApp Service Example</h3>

Example of WebApp Service ThreadLess and request without UI Thread Blocking ![example](https://github.com/thiagoschnell/webappapi/blob/main/app/src/main/java/com/after_project/webappapi/ServiceExample/WebAppServiceActivity.java), ![service source files](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/ServiceExample)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/6fa464f7-4965-4358-97ad-c7850a8222d3" />

<h3>**Zip Website Example**</h3>

Create a app and deliver the mobile version of your website, <br>
implement webappapi to make requests, <br>
load from zip your website, create a new version for the website from update.zip by url to download and install it, instant patch the website files from a external link patch.zip  <br>
Website to your Android app made easy its just need a zip file! [Zip Website files](https://github.com/after-project/zip-website), [Demo example here](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/ZipWebsiteToWebViewExample)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/7c29d543-c431-47c9-856b-6464f6a096c2" />

<h3>**Zip Website using CORS Example**</h3>

This is example of Zip Website  using CORS Example shows how setup allowed CORS domains for incoming request links in the websitewithcors.zip files. [Example here](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/ZipWebsiteToWebViewExampleWithCORS.java), [Apache MultiViews documentation](https://httpd.apache.org/docs/2.2/content-negotiation.html#multiviews)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/296e9f05-02a8-4067-9e9e-eea506a894c7" />

<h3>Api Notification Service</h3>

Note: in Android API 33 and higher you are required asking the user for grant permissions to post notifications.

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/1db44422-0140-4e68-8de4-96b8709d01b2" />

for grant permissions manual then go to Android Settings > Apps > Webapp Api> Notifications like below image:

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/c60a1d2a-45d1-411b-a0d0-6134b1f74589" />

<h3>Internet Connection - Realtime Network Status</h3>

Check the internet status in realtime before requesting any url

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/8271d9fc-846f-4509-827e-78810f3e4d9e" />

<h3>UPDATE - Job Scheduler CheckPurchaseStatus</h3>

By default BackoffPolicy.LINEAR run as 10,20,30 seconds and multiply it by x times will happen, ex: 10s *  how many times the work has been retried (for BackoffPolicy.LINEAR), and for BackoffPolicy.Exponential will be even longe.<br>
Now this example has updated to run dynamically 10,20,30 seconds.

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/479c7f78-65ab-4096-aa72-92261e73c4d1" />

<h3>Download Checksum</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0ba40731-422d-45b0-81e7-88971b4ac5db" />

<h3>Stream Download Example</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/8ff510b5-7671-4b4c-aa2f-eedfcde03146" />

<h3>Download PDF Example</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0d59ed93-aef4-4a21-9cbe-c95f3526c7d1" />

<h3>Download Image Example</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/54ee19af-486f-443f-a954-242c688d5054" />

<h3>New Response Live Data</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/e2c49232-6588-4774-a0b1-7091131ecc22" />

<h3>JobScheduler Example</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/9eeddbd6-bb88-4b25-861c-9deadf8e799b" />

<h3>LoadDataWithBaseUrl Request</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/2b7dc25f-8b1a-4854-b2c7-70c91147281c" />

<h3>Single Request</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/46e95abb-4bd2-4614-97b6-cc0c59d52330" />

<h3>On Demand Requests</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/e2bfec3a-66dd-4c24-9dc7-b3372c27e735" />


<h3>On Demand "Parallel" Requests</h3>

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/fdea0168-75a7-4b7f-96f6-f505abfb0589" />


<h3>REQUEST WITH NO INTERNET</h3>

![webapp-api-example1-connection_error](https://github.com/thiagoschnell/webappapi/assets/78884351/831a7668-d758-4bdd-bb09-7747205a7308)


<h3>LOADING WITH NO INTERNET</h3>

![webappapi-example1-load-error](https://github.com/thiagoschnell/webappapi/assets/78884351/05411355-a063-44a8-879f-168c9c6ef562)

