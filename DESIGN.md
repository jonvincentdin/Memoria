---
name: Memoria
description: Green and paper for a calm personal study space.
colors:
  forest: "#245747"
  forest-hover: "#173f32"
  forest-active: "#123428"
  paper: "#fafbf8"
  sage: "#e9efe6"
  ink: "#202a25"
  muted: "#5e6c63"
  border: "#a8b3a9"
  error: "#a33131"
  white: "#fff"
  preview-paper: "#fffefb"
  focus: "#408268"
  input-hover: "#6f8575"
  divider: "#e3e7df"
  note-surface: "#f0f3e9"
  highlight: "#d7e2bd"
  dot: "#65895d"
typography:
  display:
    fontFamily: "'DM Serif Display', serif"
    fontSize: "clamp(32px, 3vw, 46px)"
    fontWeight: 400
    lineHeight: 1.17
    letterSpacing: "-.025em"
  headline:
    fontFamily: "'DM Serif Display', serif"
    fontSize: "43px"
    fontWeight: 400
    lineHeight: 1.12
    letterSpacing: "-.025em"
  title:
    fontFamily: "'DM Serif Display', serif"
    fontSize: "26px"
    fontWeight: 400
    letterSpacing: "-.02em"
  body:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "14px"
    lineHeight: 1.7
  label:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "13px"
    fontWeight: 550
  input:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "15px"
  button:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "14px"
    fontWeight: 550
  caption:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "11px"
rounded:
  note: "3px"
  compact: "4px"
  control: "6px"
  preview: "10px"
spacing:
  compact: "6px"
  label: "9px"
  field-inline: "14px"
  page-mobile: "24px"
  form-start: "32px"
components:
  button-primary:
    backgroundColor: "{colors.forest}"
    textColor: "{colors.white}"
    typography: "{typography.button}"
    rounded: "{rounded.control}"
    width: "100%"
  button-primary-hover:
    backgroundColor: "{colors.forest-hover}"
  button-primary-active:
    backgroundColor: "{colors.forest-active}"
  input:
    backgroundColor: "{colors.white}"
    textColor: "{colors.ink}"
    typography: "{typography.input}"
    rounded: "{rounded.control}"
    padding: "12px 14px"
    width: "100%"
  study-preview:
    backgroundColor: "{colors.preview-paper}"
    rounded: "{rounded.preview}"
  preview-tab:
    textColor: "{colors.muted}"
    padding: "9px 0 10px"
  quiz-answer:
    textColor: "{colors.ink}"
    rounded: "{rounded.compact}"
    padding: "6px 10px"
---

# Design System: Memoria

## Overview

**Creative North Star: "Green and paper"**

The approved visual world pairs forest green, warm white, charcoal, an outlined book mark, serif headings, and readable sans-serif controls. It feels like a calm personal study space: generous breathing room around the form, with denser study material inside the desktop preview.

The study material itself supplies the illustration. It is built from text, CSS, and inline SVG rather than generated images. This document captures the implemented login and signup frontend; it does not establish a visual system for screens that have not been built.

**Key Characteristics:**

- Warm paper and pale sage surfaces.
- Forest green actions and understated book imagery.
- Serif headlines with clear sans-serif controls.
- A study preview on desktop and a form-first mobile layout.

## Colors

The palette combines botanical greens with warm paper and charcoal, reserving red for validation.

### Primary

- **Forest:** primary actions, links, brand, selected tabs, and form status. Deeper forest variants express hover and active states.
- **Focus green:** explicit keyboard and input focus outlines.
- **Sage:** the desktop editorial panel and selected quiz answer background.

### Neutral

- **Paper:** the page and form surface; the form is not enclosed in a floating card.
- **Preview paper:** the small study sheet on sage; white is used inside inputs and on primary button text.
- **Ink:** main text. **Muted:** secondary copy, hints, and small metadata.
- **Border:** input and quiz answer outlines. **Divider:** study tabs and reviewer rows.
- **Note surface** and **highlight:** the note excerpt and marked study terms. **Dot:** small preview indicators.
- **Error:** invalid field borders and inline error text; always accompany color with a message.

**The Paper Rule.** Keep the form directly on the warm paper surface and reserve the lifted sheet treatment for the study preview.

## Typography

**Display Font:** locally bundled DM Serif Display, with serif fallback.
**Body Font:** locally bundled variable DM Sans, with sans-serif fallback.

Serif headings give the study space a bookish character; sans-serif labels and controls keep actions readable. Fonts use swap loading. There is no monospaced role.

### Hierarchy

- **Display:** the fluid desktop editorial headline; compact desktop uses (34px).
- **Headline:** the form heading; compact desktop uses (38px), mobile (40px), and widths at or below (360px) use (36px).
- **Title:** the study preview's topic heading.
- **Body:** form introductory text; editorial body copy uses the same size with a roomier line height (1.8).
- **Label:** field labels; inputs use their separate larger role. Hints, errors, tab text, and navigation use (12px); preview metadata and footers use the caption role.
- **Brand:** DM Sans at (30px), weight (650), letter spacing (-.035em), line height (1); mobile uses (28px).

## Layout

Above (900px), two equal columns fill at least the small viewport height (100svh). Sage holds the brand, editorial copy, study preview, and footer; paper holds navigation, form, and footer. The editorial content has a maximum width (480px); the form content has a maximum width (370px), with automatic vertical margins and explicit padding so short viewports can scroll.

Desktop panel padding starts at (42px) above and (30px) below, with fluid horizontal padding. At (901–1050px), horizontal padding tightens to (32px) on the story and (36px) on the form; decorative line breaks and the preview caption disappear. At (1600px) and wider, both panels use horizontal padding (7vw).

At or below (900px), the story panel and top navigation disappear, the mobile brand appears above the form, and the layout becomes one column. Form width is capped at (390px); panel padding is (30px 24px 24px). At or below (360px), horizontal padding becomes (20px). The body supports a minimum width (320px).

Login form content uses vertical padding (62px 0 54px) on desktop; signup uses (45px 0 38px). Mobile uses (64px 0 44px), with signup top padding (44px). Labels sit (9px) above controls; field separation is (21px) for login and (16px) for signup. Preserve these observed spacings rather than forcing a new uniform scale.

## Elevation & Depth

Depth is primarily tonal: sage, paper, white inputs, and a pale note excerpt. Only the study sheet has an ambient shadow (`0 5px 12px -4px #324a3f12`) and a slight rotation (-2deg). Form controls remain flat. There are no entrance animations; control border and background transitions use (150ms ease). Reduced-motion preference removes animations and transitions.

## Shapes

Use gently curved rectangular controls, compact corners on the password toggle and quiz answers, and softer corners on the study sheet. The book mark is an inline outlined SVG, not an emoji. Small status dots are circular. Note highlighting follows the text rather than becoming a pill or badge.

## Components

### Buttons

The primary submit action is full width, forest on white text, with minimum height (49px), a centered label and arrow separated by (14px), and the control corner radius. Hover and active deepen the green. Global keyboard focus uses an outline (3px) with offset (4px). The shared disabled style uses wait cursor and opacity (.6), although the current form does not enter a pending network state.

The password visibility button is transparent with a target (44px by 44px), a small outlined eye, and a pale hover background. Password inputs reserve right padding (48px) for it.

### Cards / Containers

The study sheet is an intentionally small, slightly rotated demonstration, with the preview radius and ambient shadow. It has a heading, three tabs, and a content region with minimum height (268px). Its biology content is labeled as a preview, not as a screenshot or live saved document.

### Inputs / Fields

White inputs have minimum height (49px), a single border, the control radius, and the documented input padding. Hover darkens the border. Focus changes the border to forest and adds an outline (2px) with offset (2px). Invalid fields use the error token and a linked inline message. Labels remain visible; password hints give way to error text when invalid.

### Navigation

Login and signup use hash links (`#/login`, `#/signup`) and support browser back. Desktop account navigation sits at the upper right; a second link appears below the form on all sizes. The brand links to login. The keyboard-visible skip link focuses and scrolls the main form region without changing the route; the region suppresses its own outline so the skip action does not draw a large panel border.

### Study preview

Notes, Reviewer, and Quiz are working local tabs with selected underlines and keyboard arrow, Home, and End support. Quiz answers show a selected outline and sage fill with immediate illustrative feedback. These interactions are demonstrations, not account data or saved study progress.

### Form feedback and scope

Login contains email and password. Signup adds name and confirmation, with an eight-character password minimum; login does not apply that new-account policy. Errors focus the first invalid field. Valid input shows the explicit design-preview status. No authentication API is called, credentials are not persisted, and no successful authentication is claimed. Do not infer backend readiness from this visual documentation. Social login and recovery controls are absent because those capabilities are not implemented.

## Do's and Don'ts

### Do:

- **Do** retain the approved forest, sage, and paper palette and the serif/sans-serif relationship.
- **Do** preserve visible focus, linked inline errors, reduced-motion support, and the existing control targets.
- **Do** prioritize the form at and below the implemented mobile breakpoint.
- **Do** label illustrative study content and pending account integration honestly.

### Don't:

- **Don't** replace the approved visual world or introduce generated imagery for this auth surface.
- **Don't** add authentication success, saved-progress claims, social login, or recovery controls before the corresponding capabilities exist.
- **Don't** wrap the form in a second floating card or apply the study-sheet rotation to controls.
