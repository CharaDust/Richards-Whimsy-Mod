#include <string.h>

void CSVReadUnit(char *rslt, char *buff, int cap, int order){
	cap = cap % 256;
	int poi_n = 0,poi_o = order;
	if(order > 0){
		// find ','
		for(int fi=0;fi<256;fi++){
			if(buff[fi] == ','){
				order--;
				if(order == 0){
					poi_n = fi + 1;
					break;
				}
			}
		}
	}
	for(int poi_i=0;poi_i<cap;poi_i++){
		if(buff[poi_i+poi_n] != ','){
			rslt[poi_i] = buff[poi_i+poi_n];
		}
		else{
			rslt[poi_i] = 0;
			return;
		}
	}
}
