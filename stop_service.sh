{
    kill -9 $(ps aux | grep 'java -jar' | awk '{print $2}')
} || {
    echo '0'
}