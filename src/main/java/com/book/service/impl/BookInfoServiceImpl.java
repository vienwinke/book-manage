package com.book.service.impl;

import com.book.entity.BookInfo;
import com.book.mapper.BookInfoMapper;
import com.book.service.BookInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {
}