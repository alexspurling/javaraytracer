# Java Raycaster

![foo](/output/render.gif "Raycast rendered scene")

To render some frames to png:
`java -classpath out/production/javaraycaster javaraytracer.Raytracer record`

To convert these to a gif
`ffmpeg -framerate 50 -pattern_type glob -i 'output/*.png' -vf "fps=50,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" -loop 0 output/render.gif`
