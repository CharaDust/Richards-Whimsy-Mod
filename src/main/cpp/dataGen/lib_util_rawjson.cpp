#include<iostream>
#include<stdio.h>
#include<string>
#include<string.h>
#include <cstring> // 包含strlen和strcpy函数
#include <cstdlib> // 包含malloc和free函数

#include "lib_util_rawjson.h"

#define BPOW6 64

char* JRW_addBrct(char L_Bracket, const char* Context, char R_Bracket) {
//	std::cout<< "[lib_util_rawjson] Adding Bracket" <<std::endl;
	// 计算新字符串的长度
	int len = strlen(Context) + 3; // 左括号+Context+右括号+终止符
	
	// 分配内存空间
	char* result = (char*)malloc(len * sizeof(char));
	if (result == nullptr) {
		std::cerr << "Memory allocation failed!" << std::endl;
		return nullptr;
	}
	
	// 构建新字符串
	result[0] = L_Bracket;
	strcpy(result + 1, Context); // 将Context复制到result[1]位置开始的内存中
	result[len - 2] = R_Bracket;
	result[len - 1] = '\0'; // 添加字符串终止符
	
//	std::cout<< "[lib_util_rawjson] Added Bracket at: " << result <<std::endl;
	return result;
}
