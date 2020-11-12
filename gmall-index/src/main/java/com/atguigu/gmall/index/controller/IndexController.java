package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private IndexService indexService;
    @GetMapping("index/testLock")
    @ResponseBody
    public ResponseVo<Object> testLock(){
        indexService.testLock();
        return null;
    }

    /**
     * 获取二级分类
     * @param model
     * @return
     */
    @GetMapping("/index/cates/{pId}")
    @ResponseBody
    public ResponseVo< List<CategoryEntity>>  queryLvl2CategoriesWithSub(@PathVariable("pId") Long pId){
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl2CategoriesWithSub(pId);
        System.out.println(categoryEntities);
        return ResponseVo.ok(categoryEntities);
    }
    @GetMapping({"/","/index"})
    public String toIndex(Model model){
        //加载一级分类
        List<CategoryEntity> categoryEntities = this.indexService.queryLvl1Categories();
        //TODO :查询广告
        model.addAttribute("categories",categoryEntities);
        return "index";
    }
}
