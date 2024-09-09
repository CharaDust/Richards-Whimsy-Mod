#include<iostream>
#include<stdio.h>
#include<string>
#include<string.h>

#include "read_csv.h"
#include "lib_util_rawjson.h"
#include "state_base.h"

#define BPOW6 64
#define BPOW7 128
#define BPOW10 1024

int main(){
	std::cout<< "[Run] Progressing..." <<std::endl;
	// vars
	const static char *filename = "res/state.csv";
	static char modid[BPOW6] = "richard-whimsy-mod";
	static int row = 1;
//	int col = 0;
	static char buf[BPOW6] = "\0";
	char *fbuf = "";
	static char null[BPOW6] = "null";
	
	// main loop
	while(1){
		// READ: grop
//		std::cout<< "[Run] Reading: row[" << row << "]" <<std::endl;
		read_csv_field(filename, row, 0, buf);
		char group[8] = "";
		strcat(group,buf);
		
		// error exit
		if(!strcmp(buf,null)){
			break;
		}
		
		// READ: id
		read_csv_field(filename, row, 1, buf);
		char id[32] = "";
		strcat(id,buf);
		
		// READ: mode
		
		// gen
		char JSONname[BPOW6] = "";
		strcpy(JSONname,
			strcat(group,
				strcat(id,
					".json")));
		fbuf = genState_base(modid,group,id);
		
		char JSONpath[BPOW7] = "";
		strcpy(JSONpath,"output/blockstates/");
		strcat(JSONpath,JSONname);
		
		std::cout << "[Run] Generated for " << id << " at: \n\t" << JSONpath << std::endl;
		
		// Write: JSON Files
		
		
		
		// next one
		row++;
	}
	return 0;
}
