#version 330 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 tex;
layout(location = 2) in vec4 color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
    texCoord = tex;
    vertColor = color;
}
