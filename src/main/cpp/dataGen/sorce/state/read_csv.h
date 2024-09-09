#ifndef READ_CSV_H
#define READ_CSV_H

#define MAX_LINE_LENGTH 1024
#define MAX_FIELD_LENGTH 256

// Function to read a specific field from a CSV file
// Parameters:
// - filename: The name of the CSV file
// - target_row: The row index (0-based) of the field to read
// - target_col: The column index (0-based) of the field to read
// - result: A character array to store the result
void read_csv_field(const char *filename, int target_row, int target_col, char *result);

#endif // READ_CSV_H

