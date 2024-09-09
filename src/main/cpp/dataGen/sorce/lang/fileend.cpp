bool FileIsEnd(char *input){
	int score = 0;
	if(input[0] == 'n'){
		score++;
	}
	if(input[1] == 'u'){
		score++;
	}
	if(input[2] == 'l'){
		score++;
	}
	if(input[3] == 'l'){
		score++;
	}
	if(score == 4){
		return true;
	}
	return false;
};
