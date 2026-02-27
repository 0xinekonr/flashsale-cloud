-- stock.lua
-- KEYS[1]: 库存的 Key (例如 seckill:stock:101)
-- ARGV[1]: 需要扣减的数量 (通常是 1)

if (redis.call('exists', KEYS[1]) == 1) then
    -- 获取当前库存
    local stock = tonumber(redis.call('get', KEYS[1]));
    local num = tonumber(ARGV[1]);

    -- 判断库存是否足够
    if (stock >= num) then
        -- 扣减库存
        redis.call('decrby', KEYS[1], num);
        return 1; -- 扣减成功
    else
        return 0; -- 库存不足
    end
end

return -1; -- Key 不存在 （未预热或商品错误的异常情况）