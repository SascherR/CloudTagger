
# CloudTagger

A tag cloud based image browsing UI utilizing semantic distances provided by cortical.io�s Retina REST API.


## Installation

This is a work in progress. At the moment all included resources should be imported into your Java project using the above file structure. 

Third-party libraries are included in the library folder.

Necessary resources:

- The current tag library is stored in /res/tags.txt 
- Semantic distances retrieved by the Retina API are stored in /distances.txt
- Example images and their respective tags are stored in /images.txt

Min. requirements: Java SE 8 Update 65

## Usage

Import images of choice. Images can be tagged via dragging the desired tag out of the tag cloud onto the target image - removal of tags is possible via right-click and context menu. The tag cloud browser interface allows different �zoom� levels (1x-4x)determining the amount of steps taken while moving through the semantic distances. 

Adding new tags is currently not (yet) implemented. 


## Contributing

1. Fork it!
2. Create your feature branch: `git checkout -b my-new-feature`
3. Commit your changes: `git commit -am 'Add some feature'`
4. Push to the branch: `git push origin my-new-feature`
5. Submit a pull request!

## Credits

Sascha Rasler

## License

MIT License

Copyright (c) 2015-2016 Sascha Rasler

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
