
# WebApp Api for Android

WebApp Api is Android extended Webview Client are flexible, safe and easier request your Api and supports for using **Cross-Origin Resource Sharing (CORS)**.

This sample WebApp app uses App Message integration for send and receive messages through broadcasts.

Through the integrated AppMessage allow request Api by activity A and Return the Response to activity B or A.<br><br>
**NEWS: New integration Messenger Service are writen from zero and that will replacing the old AppMessage integration.** <br>
**next examples will be writen using LiveData Lifecycle listener to communicate between activities.**<br>

_**minimum SDK version**: Android 5.0 (API 21) (recommended), but you also can run on Android 4.4 (API 19) and higher._

## Download Android Studio and Configure

Now Webapp Api can run with your favorite android version with full compatible with latest versions:

* [Download Android Studio & Configure SDK and AGP](https://github.com/thiagoschnell/webappapi/wiki/Download-Android-Studio-&-Configure-SDK-and-AGP) This guide page for download Android Studio and Configure SDK and AGP

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

**Messenger Service Example**<img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" /> <img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" /><img alt="alt_text" width="44" src="https://github.com/thiagoschnell/webappapi/assets/78884351/b653c8bf-5247-4297-81c1-317d2ecb552a" />

In this example whe extended the application with Messenger Service to become usage with Livedata, on this listening changes they send request as messenger client and then execute it inside messenger server that are running the webapp and back it to handler click callback. ![Example usage](https://github.com/thiagoschnell/webappapi/blob/main/app/src/main/java/com/after_project/webappapi/MessengerService/MessengerServiceActivity.java), ![Messenger Service files](https://github.com/thiagoschnell/webappapi/tree/main/app/src/main/java/com/after_project/webappapi/MessengerService)

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/0db1d4da-b12c-4f38-bd41-4b296d08af07" />

**WebApp Service Example**

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

**Api Notification Service**

<img alt="alt_text" width="192px" src="https://github.com/thiagoschnell/webappapi/assets/78884351/1db44422-0140-4e68-8de4-96b8709d01b2" />

**Internet Connection - Realtime Network Status**

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

