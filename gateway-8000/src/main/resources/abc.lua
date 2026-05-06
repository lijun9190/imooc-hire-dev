local arr = {1,2,3};
for index,value in ipairs(arr) do
    print(index, value);
end

ngx.say("http协议版本：" .. ngx.req.http_version);