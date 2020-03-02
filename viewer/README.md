# Art Archive Viewer

Viewer is portable, and loadable anywhere you have [Nginx](https://www.nginx.com/).

### 1. Set the data source api 
Set `data-api` to the domain where the json API is available
```
<section id="viewer" data-api="bsu.downstreamlabs.com"></section>
```

### 2. Configure Nginx
All urls should load `index.html` at the `[domain]/viewer/index.html`.

```
server {
    listen       80;
    server_name  myproject.localhost;

    location / {
        # /var/www/html/project/viewer/README.md [you are here]
        root   /var/www/html/project;
        index  index.html /viewer/index.html;

        location /viewer/css {}
        location /viewer/js {}

        try_files /viewer/index.html =404;
    }
}
```