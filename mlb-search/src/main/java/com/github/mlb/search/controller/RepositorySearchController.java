//package com.github.mlb.search.controller;
//
//import com.github.mlb.search.biz.manager.RepositoryManager;
//import com.github.mlb.search.biz.repository.entity.RepositoryDocument;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// * @author JiHongYuan
// * @date 2021/9/18 15:48
// */
//
//@RestController
//public class RepositorySearchController {
//    private RepositoryManager repositoryManager;
//
//    @GetMapping
//    public Object find(String repositoryName, String desc) {
//        return repositoryManager.findByRepositoryNameAndAndDesc(repositoryName, desc);
//    }
//
//}