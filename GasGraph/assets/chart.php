<?php

if ( array_key_exists( 'url', $_POST ) ){

    $path = $_POST['url'];
    $mm_type="image/png"; // modify accordingly type of
    if ( preg_match("/^http:\/\/chart.apis.google.com/", $path) == 0 ) {
        //echo "no match\n";
        //echo $path. "\n";
        exit();
    }
    $url_fd = fopen($path, 'rb');

    header("Pragma: public");
    header("Expires: 0");
    header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    header("Cache-Control: public");
    header("Content-Description: File Transfer");
    header("Content-Type: " . $mm_type);
    //header("Content-Length: " .(string)(filesize($path)) );
    //header('Content-Disposition: attachment; filename="'.basename($path).'"');
    header("Content-Transfer-Encoding: binary\n");
    fpassthru($url_fd); // outputs the content of the file
    //fclose($furl_fd);
}

exit();
?>
