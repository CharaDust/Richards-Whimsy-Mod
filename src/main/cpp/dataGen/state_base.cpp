#include<iostream>
#include<stdio.h>
#include<string>
#include<string.h>
#include <cstring> // 包含strlen和strcpy函数
#include <cstdlib> // 包含malloc和free函数

#define BPOW8 256
#define BPOW10 1024
#define SAB JRW_addBrct
#define SLK strcat

#include "state_base.h"
#include "lib_util_rawjson.h"


char* genState_base(char* MODID, char* Group, char* id){
	int len = BPOW10;
	
	// 分配内存空间
	char* result = (char*)malloc(len * sizeof(char));
	if (result == nullptr) {
		std::cerr << "Memory allocation failed!" << std::endl;
		return nullptr;
	}
	
//	std::cout<< "[state_base] Generating VarText" <<std::endl;
	// 生成变量文本
	char varTxt[BPOW8] = "";
	strcpy(varTxt, MODID);
	strcat(varTxt, ":");
	strcat(varTxt, "block/");
	strcat(varTxt, Group);
	strcat(varTxt, "_");
	strcat(varTxt, id);
	
//	std::cout<< "[state_base] Generating Whole Struct" <<std::endl;
	
	// 生成整体结构
	/*
	result = JRW_addBrct(
		'{',
		strcat(
			strcat(
				JRW_addBrct(
					34,
					"variants",
					34),
				":"),
			JRW_addBrct(
				'{',
				strcat(
					strcat(
						JRW_addBrct(
							34,
							"",
							34),
						":"),
					JRW_addBrct(
						'{',
						strcat(
							strcat(
								JRW_addBrct(
									34,
									"model",
									34),
								":"),
							JRW_addBrct(34,varTxt,34)),
						'}')),
				'}')),
		'}');
	*/
	
	// 简化版结构
	char constHead[128] = "\"variants\": {\"\": {\"model\":";
	result = SAB(
		'{',
		SLK(SLK(constHead,SAB(34,varTxt,34)),"}}"),
		'}');
//	std::cout<< "[state_base] All Done!!!" <<std::endl;
	
	return result;
}

