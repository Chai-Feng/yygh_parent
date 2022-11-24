package com.alex.yygh.common.exception;

import com.alex.yygh.common.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Title:
 * @Description: TODO
 * @author: Alex
 * @Version:
 * @date 2022-11-08-20:25
 */

@ControllerAdvice
@ResponseBody //结果以json格式输出
public class GlobalException {

    @ExceptionHandler(Exception.class)
    public Result error(Exception e) {
        e.printStackTrace();
        return Result.fail();
    }



    @ExceptionHandler(YyghException.class)
    public Result error(YyghException e) {
        e.printStackTrace();
        return Result.fail();
    }
}
