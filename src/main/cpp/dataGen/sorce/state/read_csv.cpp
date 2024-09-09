#include <iostream>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "read_csv.h"

#define MAX_LINE_LENGTH 1024
#define MAX_FIELD_LENGTH 256

void read_csv_field(const char *filename, int target_row, int target_col, char *result) {
	FILE *file = fopen(filename, "r");
	if (!file) {
		perror("Error opening file");
		exit(EXIT_FAILURE);
	}
	
	char line[MAX_LINE_LENGTH];
	int current_row = 0;
	
	while (fgets(line, sizeof(line), file)) {
		if (current_row == target_row) {
			int current_col = 0;
			char *token = strtok(line, ",");
			while (token) {
				if (current_col == target_col) {
					strncpy(result, token, MAX_FIELD_LENGTH - 1);
					result[MAX_FIELD_LENGTH - 1] = '\0'; // Ensure null termination
					fclose(file);
					return;
				}
				token = strtok(NULL, ",");
				current_col++;
			}
			break;
		}
		current_row++;
	}
	
	// If the specified row or column is not found
	strcpy(result, "Field not found");
	fclose(file);
}

/*
int main() {
	const char *filename = "data/data.csv";
	int target_row = 2;  // specify the target row (0-based index)
	int target_col = 1;  // specify the target column (0-based index)
	char result[MAX_FIELD_LENGTH];
	
	read_csv_field(filename, target_row, target_col, result);
	printf("The field at row %d, column %d is: %s\n", target_row, target_col, result);
	
	return 0;
}
*/
