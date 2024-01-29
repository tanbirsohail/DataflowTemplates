/*
 * Copyright (C) 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package terraform

import (
	"bytes"
	"errors"
	"github.com/google/go-cmp/cmp"
	"github.com/google/go-cmp/cmp/cmpopts"
	"github.com/hashicorp/hcl/v2"
	"github.com/hashicorp/hcl/v2/hclsyntax"
	tfjson "github.com/hashicorp/terraform-json"
	"github.com/zclconf/go-cty/cty"
	"strings"
	"testing"
)

func TestEncoder_Encode_header(t *testing.T) {
	enc := &Encoder[*tfjson.ProviderSchema]{
		tmplName: headerTmpl,
	}
	buf := bytes.Buffer{}
	if err := enc.Encode(&buf, map[string]*tfjson.ProviderSchema{}); err != nil {
		t.Fatal(err)
	}

	got := buf.String()
	if !strings.Contains(got, `# Autogenerated file. DO NOT EDIT.`) {
		t.Errorf("Encode() mismatch found of %s", got)
	}
}

func TestEncoder_Encode_variables(t *testing.T) {
	tests := []struct {
		name  string
		input map[string]*tfjson.SchemaAttribute
		want  string
	}{
		{
			name: "zero value",
			input: map[string]*tfjson.SchemaAttribute{
				"foo": {},
			},
			want: `
				variable "foo" {
				}
			`,
		},
		{
			name: "string type",
			input: map[string]*tfjson.SchemaAttribute{
				"foo": {
					AttributeType: cty.String,
				},
			},
			want: `
				variable "foo" {
					type = string
				}`,
		},
		{
			name: "map(number) type",
			input: map[string]*tfjson.SchemaAttribute{
				"foo": {
					AttributeType: cty.Map(cty.Number),
				},
			},
			want: `
			  	variable "foo" {
					type = map(number)
				}
			`,
		},
		{
			name: "with description",
			input: map[string]*tfjson.SchemaAttribute{
				"foo": {
					Description: "some description",
				},
			},
			want: `
				variable "foo" {
					description = "some description"
				}`,
		},
		{
			name: "two variables",
			input: map[string]*tfjson.SchemaAttribute{
				"foo": {},
				"bar": {},
			},
			want: `
				variable "bar" {
				}
				variable "foo" {
				}`,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			buf := bytes.Buffer{}
			enc := &Encoder[*tfjson.SchemaAttribute]{
				tmplName: variablesTmpl,
			}
			if err := enc.Encode(&buf, tt.input); err != nil {
				t.Fatal(err)
			}

			diff("variables.tmpl", tt.want, buf, t)
		})
	}
}

func TestEncoder_Encode_module(t *testing.T) {
	tests := []struct {
		name  string
		input map[string]*tfjson.Schema
		want  string
	}{
		{
			name:  "empty",
			input: map[string]*tfjson.Schema{},
			want:  "",
		},
		{
			name: "no attrs",
			input: map[string]*tfjson.Schema{
				"foo": {
					Block: &tfjson.SchemaBlock{},
				},
			},
			want: `resource "foo" "generated" {}`,
		},
		{
			name: "1 attr",
			input: map[string]*tfjson.Schema{
				"foo": {
					Block: &tfjson.SchemaBlock{
						Attributes: map[string]*tfjson.SchemaAttribute{
							"bar": {},
						},
					},
				},
			},
			want: `
			variable "bar" {}
			resource "foo" "generated" {
				bar = var.bar
			}`,
		},
		{
			name: "2 attrs",
			input: map[string]*tfjson.Schema{
				"foo": {
					Block: &tfjson.SchemaBlock{
						Attributes: map[string]*tfjson.SchemaAttribute{
							"bar": {},
							"baz": {},
						},
					},
				},
			},
			want: `
			variable "bar" {}
			variable "baz" {}
			resource "foo" "generated" {
				bar = var.bar
				baz = var.baz
			}`,
		},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			buf := bytes.Buffer{}
			enc := &Encoder[*tfjson.Schema]{
				tmplName: moduleTmpl,
			}
			if err := enc.Encode(&buf, tt.input); err != nil {
				t.Fatal(err)
			}

			diff("module.tmpl", tt.want, buf, t)
		})
	}
}

func diff(message string, want string, got bytes.Buffer, t *testing.T) {
	wf, wd := hclsyntax.ParseConfig([]byte(want), message, hcl.InitialPos)
	if wd.HasErrors() {
		t.Fatal(errors.Join(wd.Errs()...))
	}

	gf, gd := hclsyntax.ParseConfig(got.Bytes(), message, hcl.InitialPos)
	if gd.HasErrors() {
		t.Fatal(errors.Join(gd.Errs()...))
	}

	opts := []cmp.Option{
		cmpopts.IgnoreUnexported(
			hclsyntax.Body{},
			hclsyntax.ScopeTraversalExpr{},
			hcl.TraverseRoot{},
			hclsyntax.TemplateExpr{},
			hclsyntax.LiteralValueExpr{},
			hcl.TraverseAttr{},
		),
		cmpopts.IgnoreTypes(hcl.Range{}),
		cmp.AllowUnexported(cty.Value{}, cty.Type{}),
		cmpopts.EquateComparable(cty.Type{}),
	}

	if diff := cmp.Diff(wf.Body, gf.Body, opts...); diff != "" {
		t.Errorf("%s mismatch (-want,+got)\n%s", message, diff)
	}
}
