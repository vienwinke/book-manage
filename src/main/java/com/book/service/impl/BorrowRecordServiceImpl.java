package com.book.service.impl;

import com.book.entity.BorrowRecord;
import com.book.mapper.BorrowRecordMapper;
import com.book.service.BorrowRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BorrowRecordServiceImpl extends ServiceImpl<BorrowRecordMapper, BorrowRecord> implements BorrowRecordService {

}