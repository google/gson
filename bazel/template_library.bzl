def _template_library_impl(ctx):
    file = ctx.actions.declare_file(
        "_virtual_imports/{target_name}/java/{file}".format(
            target_name = ctx.attr.name,
            file = ctx.file.template.path,
        ),
    )
    ctx.actions.expand_template(
        template = ctx.file.template,
        output = file,
        substitutions = ctx.attr.substitutions,
    )

    return [
        DefaultInfo(
            files = depset([file]),
        ),
    ]

template_library = rule(
    implementation = _template_library_impl,
    attrs = {
        "substitutions": attr.string_dict(
            mandatory = True,
        ),
        "template": attr.label(
            mandatory = True,
            allow_single_file = True,
        ),
    },
)
