#version 330 core
// yoinked from https://github.com/mhalber/Lines/blob/master/geometry_shader_lines.h

layout(location = 0) in vec4 pos_width;
layout(location = 1) in vec4 color;

uniform mat4 projection;
uniform mat4 model;

out vec4 v_col;
noperspective out float v_line_width;

void main() {
    gl_Position = projection * model * vec4(pos_width.xyz, 1.0);
    v_col = color;
    v_line_width = pos_width.w;
}
