#version 330 core

layout(location = 0) in vec4 clip_pos;
layout(location = 1) in vec4 color;
layout(location = 2) in vec4 dist;

out vec4 vertColor;
noperspective out vec4 vertDist;

void main() {
    gl_Position = clip_pos;
    vertColor = color;
    vertDist = dist;
}
