# Insane Noises Music Generator

This is the final group project done for COSC-452 Seminar in Computer Science: Evolutionary Computation at Amherst College where we were allowed to do anything as long as it utilized evolutionary computation principles. The code was written in collaboration with Lemara Williams, Class of 2022, and April Dotton-Carter, Class of 2023 (this README for my personal GitHub is being written by myself, Karen Liu, Class of 2023). Further details about what code was written by myself is provided below in the Personal Contributions section.

## Contents

-   Overview: Aims, Activities, Products

-   Detailed Description of the System

-   Personal Contributions and Future Work

## Overview: Aims, Activities, Products

Our group set out to create a music evolution system that could seamlessly integrate with a visual simulation that animates the music it is given, the best final individual from the system. Our activities can be broken down into two major components --- working on the evolutionary system and working on the visual simulation. These two aspects worked in parallel. While the evolutionary half progressed steadily from the beginning based on Lemara's original code, the visual simulation half of things had two major stages --- coming up with ideas for the visual and then actually coding it. Our results are a functional system that produces music, produces a visual, and can essentially stand alone, though of course there is much more that can be done --- for instance, we should test more parameters that affect variation and add more cases to the error function in order to make "better" music; we could also consider a wholly different visual system that may achieve better intuitive connection to the music than our current one.

## Detailed Description of the System

The evolutionary system starts with 50 individuals of genome length 65 that will go through 100 generations of variation and selection. Each gene in the individuals is a random MIDI note between 36 and 84, with a random duration of ½, 1, 2, or 4 to represent eighth notes, quarter notes, half notes, and whole notes.

The variation method is a combination of mutation and crossover. The mutation is a looser version of UMAD in that it does retain uniformity over time, but rather than performing both an addition and a deletion sweep in the same generation, there is a 50% chance for each addition and deletion so that only one type of sweep is performed per generation. This allows for greater fluctuations in genome length, which we hoped would be helpful for keeping the sounds interesting. The crossover function is essentially the one in Propeller, but it swaps genes 50% of the time, which is a higher frequency than in Propeller.

The selection method is lexicase selection, and our error function tests for genome length on a hyperbolic shape so that genome lengths will be penalized dramatically if they are shorter than or closer to 30. This way, the error will never be zero, which is why our system always runs through the max number of generations.

![](https://lh4.googleusercontent.com/EyO2Ve_M6sh3DrnmRJWX6ykAkjfN_C0xqtveuL4qWacnwQ6UasMo6SJvL3z3tit5F4PXU3VRiYgEp9uUiLfHmOSdrMpbvZjq5qHXhDeV9JanFTaDX8nx_TF05vikk7QNBk_vizRFnYTTs7V_nr9Mbw)

The other characteristic our error function tests for is the note intervals, penalizing tritones, octave jumps, and intervals over a fifth a certain percentage of the time and leaving them unpenalized the rest of the time. The reason we decided to penalize these intervals is because we wanted to abide by classical music conventions as an initial basis for achieving "pleasing" sounds since there is no other clear set of rules we could implement in code without going way beyond the scope of a single month. In order to avoid a boring, strict adherence to these rules, we implemented them so that these rules are not penalized all the time in order to allow for some intrigue.

The visual system aims to create an animation of contracting and expanding circles that vaguely imitate the pulsing of a loud speaker. Each characteristic is keyed to an element of the visual, like so:

-   Radius MIDI number, or pitch

-   Color (Hue) MIDI number, or pitch

-   Color (Brightness) How many times that same note has been hit

-   Duration of contraction Duration of note

The circle will appear at the note's location and leave a colored "imprint" of itself at the beginning of every note and it will contract until the beginning of the next note. When the same note is hit again, the imprint will become brighter and brighter. The final product should look like almost like a record, with a clear, colored distribution of circle imprints that show what the overall "shape" of the song was like. These images can be seen in the presentation slides, which are attached as a separate PDF. Videos of some of the more interesting runs are also included.

## Personal Contributions and Future Work

My main roles included creating the visual half of the system, debugging and making modifications to the evolutionary half of the system, and merging the two systems together after we'd finished our respective halves; I also did all the testing and tweaking of the merged system, recorded the visual results of runs, and organized the results and future work sections of the presentation.

For the visual half of the system, I created the code from scratch based on my past exploration of the bouncing circles animation in Quil and wrote the structure of the entire file. It's structured so that every single characteristic, listed earlier, and the necessary information to code these visual qualities are keys in a map, and this map is the one that is passed from (setup [ ]) and between (update-state [state]) and (draw [state]). The only code that wasn't written by me were the keys and draw commands related to the brightness of the circles (which represent the amount of times that note has been hit).

For the evolutionary system, I debugged and cleaned up the code, which is the version in this repo. I also modified the error function to penalize genome length hyperbolically instead of with an absolute cutoff of which lengths are desirable and which are not. For variation, I created the mutation function based on old code from your class examples and added the crossover function to the system from Propeller.

Merging the system was difficult, as we ran into problems with getting Overtone to work after it had combined with the visual system. A teammate and I worked together on merging the system, and I eventually put Dynne in since I'd used it in past projects and Overtone did not seem to have any hope of working.

Testing was straightforward --- it was just manipulating many of the changeable parameters to approach a more "pleasing" set of final individuals. Parameters I tested included:

-   Initial genome size

-   Minimum desirable genome size in the error function

-   Ratio of crossover to mutation

-   Frequency of addition and deletion within mutation

-   Frequency of gene swapping in crossover

-   Frequency of allowing tritones, octaves, and jumps over a fifth to go unpenalized

Other things I want to test in the future, including the ones I've already listed:

-   Generation number

-   Initial population size

I'll include in the submission photos and videos that were in the presentation but couldn't be played during class due to technical difficulties. I won't reiterate future work because it was already talked about on the last slide of the presentation.

Other notes on future work and observations that weren't included in the presentation, however, include:

-   The best genome is always too short, and it seems like the system is prioritizing size over intervals.

-   Changing the color to key to the order in which it was drawn would preserve the single key to single value standard, and it may also show how the song plays over time; a time-tracked history of the notes played is not something currently seen in the final visual if only looking at the last image as opposed to the entire animation.

-   Long genomes look cooler but take forever to draw.

-   Batch testing on a cluster would be good if we had analysis scripts, but there's the question of how to get analysis scripts to capture the nuances of human evaluation and aesthetic judgment.

-   Unsure of how to control for genome length without being too restrictive or too permissive.

-   We should include in the error function a test on the variance of note lengths as well as note frequencies to modulate skips and steps, as well as rhythmic interest.

## Usage

Downloading the repo and running `core.clj` in the repo will produce a window that plays sound and shows the ring animation.

## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
