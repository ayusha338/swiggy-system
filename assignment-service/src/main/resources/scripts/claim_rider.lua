local currentStatus = redis.call('GET', KEYS[1])
if currentStatus == ARGV[1] then
    redis.call('SET', KEYS[1], ARGV[2])
    return 1
else
    return 0
end