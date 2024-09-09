#include <stdio.h>
#include <string.h>
#include <fileapi.h>
#include "fileend.cpp"
#include "csvread.cpp"

char buff[255];
char regname[255], group[5], type[16], cstmnamespace[32];
int line = 0, deal_type = 0;

int main(){
	FILE *readin = NULL, *readrec = NULL;
	readin = fopen("../../src/reglist.csv", "r");
	if (readin == NULL) 
	{  
		perror("Error opening file");  
		return -1;  
	}  
	readrec = readin;
	// Main Loop
	while(1){
		// Read ---into--> Buff
		fgets(buff,255,(FILE *)readin);
		printf("Line:%d\n",line);
		
		// File end
		if(FileIsEnd(buff)){
			printf("FILE END!!!");
			break;
		}
		
		// Buff ---into--> data
		if(line > 0){
			// cstmnamespace
			CSVReadUnit(cstmnamespace,buff,255,0);
			printf("%s\t",cstmnamespace);
		
			// type
			CSVReadUnit(type,buff,255,1);
			printf("%s\t",type);
			
			// group
			CSVReadUnit(group,buff,255,2);
			printf("%s\t",group);
			
			// regname
			CSVReadUnit(regname,buff,255,3);
			printf("%s\t",regname);
		}
		
		printf("\n");
		strcpy(buff,"");
		strcpy(cstmnamespace,"");
		strcpy(type,"");
		strcpy(group,"");
		strcpy(regname,"");
		line ++;
	}
	line = 0;
	return 0;
}
