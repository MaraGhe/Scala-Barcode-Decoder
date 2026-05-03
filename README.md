## EAN-13 Barcode Decoder

*Author:* Gheorghe Mara, Politehnica University of Bucharest

This project implements a simplified EAN-13 barcode decoder in Scala. It processes black-and-white bit matrices (where 1 is black and 0 is white) to identify products by calculating relative bar widths and matching them against standard L, G, and R encodings.

*All testing & main program logic, as well as the helper functions for reading barcodes were implemented by the Programming Paradigms team at the university*

---

## Decoder Implementation

The decoder logic is located in the `Decoder.scala` file. Below is a brief description of the functions implemented to facilitate the decoding process.

### 1. Elementary Functions
*   **`toBit(s: Char / Int)`**: Converts characters or integers into a `Bit` type.
*   **`complement(c: Bit)`**: Returns the logical opposite of a given bit.
*   **`leftOddList / rightList / leftEvenList`**: Generates the standard binary representations for L (left-odd), R (right), and G (left-even) encodings using higher-order functions.
*   **`group[A](l: List[A])`**: Groups identical consecutive elements into separate sub-lists.
*   **`runLength[A](l: List[A])`**: Compresses a list into a sequence of tuples containing the element and the number of consecutive occurrences.

### 2. Rational Number Arithmetic 
*   **`-, +, *, /`**: Implements standard arithmetic operations for fractions to maintain precision during bar width calculations.
*   **`compare(other: RatioInt)`**: Compares two fractions, returning -1, 0, or 1 based on their relationship.

### 3. Input Transformation
*   **`scaleToOne[A]`**: Converts absolute bar counts into relative frequencies (proportions) based on the total width of the sequence.
*   **`scaledRunLength`**: Transforms a run-length encoded list into a tuple containing the starting bit and a list of relative bar sizes.
*   **`toParities`**: Converts a string representation of parities (L/G) into a list of `Parity` types.
*   **`leftParityList`**: A collection of valid parity patterns used to determine the first digit of the EAN-13 code.
*   **`leftOddSRL / leftEvenSRL / rightSRL`**: Pre-calculates the relative scale versions of the standard digit encodings for fast comparison.

### 4. Digit Identification
*   **`distance(l1: SRL, l2: SRL)`**: Calculates the similarity between two encodings by summing the absolute differences of their relative bar widths.
*   **`bestMatch`**: Iterates through a set of standard encodings to find the digit with the minimum distance to the input segment.
*   **`bestLeft / bestRight`**: Returns the parity and best match for a digit in the left / right group.
*   **`findLast12Digits`**: Segments the 59-bar sequence (ignoring start, center, and end guards) into 12 individual digits.
*   **`firstDigit`**: Determines the first digit of the barcode by analyzing the parity pattern of the left group of digits.
*   **`checkDigit`**: Calculates the EAN-13 checksum (the last digit) based on the first 12 identified digits.
*   **`verifyCode`**: Validates the complete 13-digit sequence by checking the parity pattern and the control digit.
*   **`solve`**: The main entry point that takes a run-length encoded row and returns the final barcode string if it is valid.

### Testing
Run the `Test.scala` file inside the `src/test` folder to check  the more complex functions

To run any barcode, locate the `MyBarcodes.scala` file. The `readBarcodes` function accepts the name of an input folder and an output folder. 
In the input folder, you can place your own barcode images in `.ppm` format. Ensure you crop the images to include only the barcode and verify they are clear enough; otherwise, the algorithm will fail. You can convert images to the correct format using [convertio.co](https://convertio.co/). 

In the output folder, you will find the same images converted into a black-and-white format (`.pbm`).
